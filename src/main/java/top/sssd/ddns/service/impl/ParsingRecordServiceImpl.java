package top.sssd.ddns.service.impl;

import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.sssd.ddns.common.BizException;
import top.sssd.ddns.common.enums.RecordTypeEnum;
import top.sssd.ddns.common.enums.ServiceProviderEnum;
import top.sssd.ddns.common.enums.UpdateFrequencyEnum;
import top.sssd.ddns.common.utils.PageUtils;
import top.sssd.ddns.factory.DynamicDnsServiceFactory;
import top.sssd.ddns.mapper.ParsingRecordMapper;
import top.sssd.ddns.model.entity.JobTask;
import top.sssd.ddns.model.entity.ParsingRecord;
import top.sssd.ddns.service.DynamicDnsService;
import top.sssd.ddns.service.IJobTaskService;
import top.sssd.ddns.service.IParsingRecordService;
import top.sssd.ddns.task.DynamicDnsJob;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static top.sssd.ddns.common.constant.DDNSConstant.*;

/**
 * <p>
 * 解析记录表 服务实现类
 * </p>
 *
 * @author sssd
 * @since 2023-03-19
 */
@Service
@Slf4j
public class ParsingRecordServiceImpl extends ServiceImpl<ParsingRecordMapper, ParsingRecord> implements IParsingRecordService {

    @Resource
    private IJobTaskService jobTaskService;

    @Override
    public void add(ParsingRecord parsingRecord) throws Exception {
        DynamicDnsService dynamicDnsService = DynamicDnsServiceFactory.getServiceInstance(parsingRecord.getServiceProvider());

        String ip = null;
        Integer getIpMode = parsingRecord.getGetIpMode();
        if (getIpMode.equals(IP_MODE_INTERFACE)) {
            ip = getIp(parsingRecord);
        }else if(getIpMode.equals(IP_MODE_NETWORK)){
            ip = parsingRecord.getIp();
        }

        //后端唯一性校验
        ParsingRecord checkParsingRecord = this.lambdaQuery()
                .eq(ParsingRecord::getServiceProvider, parsingRecord.getServiceProvider())
                .eq(ParsingRecord::getRecordType, parsingRecord.getRecordType())
                .eq(ParsingRecord::getDomain, parsingRecord.getDomain())
                .eq(ParsingRecord::getIp, parsingRecord.getIp())
                .last("limit 1").one();
        if (Objects.nonNull(checkParsingRecord)) {
            throw new BizException("同一服务商,同一解析类型,同一ip,不能重复添加");
        }
        if (dynamicDnsService.exist(parsingRecord.getServiceProviderId(),
                parsingRecord.getServiceProviderSecret(),
                parsingRecord.getDomain(),
                RecordTypeEnum.getNameByIndex(parsingRecord.getRecordType()))) {
            throw new BizException("该记录已在域名服务商中存在");
        }
        dynamicDnsService.add(parsingRecord, ip);
        this.save(parsingRecord);
        //添加并启动一个定时任务
        addWithStartTask(parsingRecord);
    }

    @Override
    public void modify(ParsingRecord parsingRecord) throws Exception {
        DynamicDnsService dynamicDnsService = DynamicDnsServiceFactory.getServiceInstance(parsingRecord.getServiceProvider());

        ParsingRecord dbParsingRecord = this.getById(parsingRecord.getId());
        if (Objects.isNull(dbParsingRecord)) {
            throw new BizException("该记录不存在");
        }
        //后端唯一性校验
        ParsingRecord checkParsingRecord = this.lambdaQuery()
                .eq(ParsingRecord::getServiceProvider, parsingRecord.getServiceProvider())
                .eq(ParsingRecord::getRecordType, parsingRecord.getRecordType())
                .eq(ParsingRecord::getIp, parsingRecord.getIp())
                .eq(ParsingRecord::getDomain, parsingRecord.getDomain())
                .ne(ParsingRecord::getId, parsingRecord.getId())
                .last("limit 1").one();
        if (Objects.nonNull(checkParsingRecord)) {
            throw new BizException("同一服务商,同一解析类型,同一ip,不允许重复更新");
        }

        // 删除之前的定时任务
        JobTask one = jobTaskService.lambdaQuery().eq(JobTask::getName, dbParsingRecord.getId().toString()).one();
        if (Objects.nonNull(one)) {
            jobTaskService.deleteJobTask(one.getId());
        }

        if (parsingRecord.getState().equals(0)) {
            this.updateById(parsingRecord);
            return;
        }

        String dnsIp = null;
        try {
            dnsIp = dynamicDnsService.getIpBySubDomainWithType(dbParsingRecord);
        } catch (TencentCloudSDKException e) {
            e.printStackTrace();
        }
        String recordId = dynamicDnsService.getRecordId(dbParsingRecord, dnsIp);

        String ip = null;
        Integer getIpMode = parsingRecord.getGetIpMode();
        if (getIpMode.equals(IP_MODE_INTERFACE)) {
            ip = getIp(parsingRecord);
        }else if(getIpMode.equals(IP_MODE_NETWORK)){
             ip = parsingRecord.getIp();
        }

        dynamicDnsService.update(parsingRecord, ip, recordId);
        this.updateById(parsingRecord);
        // 添加并启动一个定时任务
        addWithStartTask(parsingRecord);
    }

    private void addWithStartTask(ParsingRecord parsingRecord) {
        JobTask jobTask = new JobTask();
        jobTask.setName(parsingRecord.getId().toString());
        jobTask.setStatus(1);
        jobTask.setClassName(DynamicDnsJob.class.getName());
        jobTask.setCronExpression(UpdateFrequencyEnum.getCronExpressionByCode(parsingRecord.getUpdateFrequency()));
        jobTask.setExecuteParams(parsingRecord);
        jobTaskService.addJobTask(jobTask);
    }

    @Override
    public void delete(Long id) throws Exception {
        ParsingRecord parsingRecord = this.getById(id);
        if (Objects.isNull(parsingRecord)) {
            throw new BizException("该记录不存在");
        }
        DynamicDnsService dynamicDnsService = DynamicDnsServiceFactory.getServiceInstance(parsingRecord.getServiceProvider());

        if (!dynamicDnsService.exist(parsingRecord.getServiceProviderId(),
                parsingRecord.getServiceProviderSecret(),
                parsingRecord.getDomain(),
                RecordTypeEnum.getNameByIndex(parsingRecord.getRecordType()))) {
            throw new BizException("该记录在域名服务商中不存在");
        }
        String ip = null;
        Integer getIpMode = parsingRecord.getGetIpMode();
        if (getIpMode.equals(IP_MODE_INTERFACE)) {
            ip = getIp(parsingRecord);
        }else if(getIpMode.equals(IP_MODE_NETWORK)){
            ip = parsingRecord.getIp();
        }
        dynamicDnsService.remove(parsingRecord, ip);
        this.removeById(id);
        //  2023/5/2 删除定时任务
        JobTask one = jobTaskService.lambdaQuery().eq(JobTask::getName, parsingRecord.getId()).one();
        if (Objects.nonNull(one)) {
            jobTaskService.deleteJobTask(one.getId());
        }
    }

    @Override
    public PageUtils<ParsingRecord> queryPage(ParsingRecord parsingRecord) {
        Page<ParsingRecord> page = lambdaQuery()
                .eq(Objects.nonNull(parsingRecord.getServiceProvider()), ParsingRecord::getServiceProvider, parsingRecord.getServiceProvider())
                .eq(StringUtils.hasText(parsingRecord.getDomain()), ParsingRecord::getDomain, parsingRecord.getDomain())
                .eq(Objects.nonNull(parsingRecord.getRecordType()), ParsingRecord::getRecordType, parsingRecord.getRecordType())
                .eq(Objects.nonNull(parsingRecord.getState()), ParsingRecord::getState, parsingRecord.getState())
                .ge(Objects.nonNull(parsingRecord.getCreateDate()), ParsingRecord::getCreateDate, parsingRecord.getCreateDate())
                .le(Objects.nonNull(parsingRecord.getUpdateDate()), ParsingRecord::getUpdateDate, parsingRecord.getUpdateDate())
                .page(new Page<ParsingRecord>(parsingRecord.getPage(), parsingRecord.getPageSize()));
        List<ParsingRecord> resultList = page.getRecords().stream().map(item -> {
            String serviceProviderName = ServiceProviderEnum.getNameByIndex(item.getServiceProvider());
            String recordTypeName = RecordTypeEnum.getNameByIndex(item.getRecordType());
            item.setServiceProviderName(serviceProviderName);
            item.setRecordTypeName(recordTypeName);
            return item;
        }).collect(Collectors.toList());
        page.setRecords(resultList);
        return new PageUtils<ParsingRecord>(page);
    }

    @Override
    public String getIp(ParsingRecord parsingRecord) {
        //解析类型:1 AAAA 2 A
        Integer recordType = parsingRecord.getRecordType();
        if (recordType.equals(RECORD_TYPE_AAAA)) {
            //ipv6
            String ipv6Interface = Arrays.stream(IPV6_INTERFACE_VALUES).findAny().get();
            parsingRecord.setGetIpModeValue(ipv6Interface);
            parsingRecord.setRecordType(RECORD_TYPE_AAAA);
            String ipv6 = HttpUtil.get(ipv6Interface);
            parsingRecord.setIp(ipv6);
            return ipv6.trim();
        } else if (recordType.equals(RECORD_TYPE_A)) {
            //ipv4
            String ipv4Interface = Arrays.stream(IPV4_INTERFACE_VALUES).findAny().get();
            parsingRecord.setGetIpModeValue(ipv4Interface);
            parsingRecord.setRecordType(RECORD_TYPE_A);
            String ipv4 = HttpUtil.get(ipv4Interface);
            parsingRecord.setIp(ipv4);
            return ipv4.trim();
        }
        return null;
    }
}
