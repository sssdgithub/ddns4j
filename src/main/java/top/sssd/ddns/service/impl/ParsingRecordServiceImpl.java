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
import top.sssd.ddns.common.utils.AmisPageUtils;
import top.sssd.ddns.factory.DynamicDnsServiceFactory;
import top.sssd.ddns.mapper.ParsingRecordMapper;
import top.sssd.ddns.model.entity.JobTask;
import top.sssd.ddns.model.entity.ParsingRecord;
import top.sssd.ddns.model.response.NetWorkSelectResponse;
import top.sssd.ddns.service.DynamicDnsService;
import top.sssd.ddns.service.IJobTaskService;
import top.sssd.ddns.service.IParsingRecordService;
import top.sssd.ddns.service.NetWorkService;
import top.sssd.ddns.task.DynamicDnsJob;

import javax.annotation.Resource;
import java.net.SocketException;
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

        String ip = getIp(parsingRecord);

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

        String ip = getIp(parsingRecord);

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
        String ip = getIp(parsingRecord);
        dynamicDnsService.remove(parsingRecord, ip);
        this.removeById(id);
        //  2023/5/2 删除定时任务
        JobTask one = jobTaskService.lambdaQuery().eq(JobTask::getName, parsingRecord.getId().toString()).one();
        if (Objects.nonNull(one)) {
            jobTaskService.deleteJobTask(one.getId());
        }
    }

    @Override
    public String getIp(ParsingRecord parsingRecord) {
        //解析类型:1 AAAA 2 A
        Integer recordType = parsingRecord.getRecordType();
        if (recordType.equals(RECORD_TYPE_AAAA)) {
            //ipv6
            if(parsingRecord.getGetIpMode().equals(IP_MODE_INTERFACE)){
                String ipv6 = HttpUtil.get(parsingRecord.getGetIpModeValue().trim());
                parsingRecord.setIp(ipv6);
                return ipv6.trim();
            }else if(parsingRecord.getGetIpMode().equals(IP_MODE_NETWORK)){
                parsingRecord.setIp(parsingRecord.getGetIpModeValue());
                return parsingRecord.getGetIpModeValue().trim();
            }
        } else if (recordType.equals(RECORD_TYPE_A)) {
            //ipv4
            if(parsingRecord.getGetIpMode().equals(IP_MODE_INTERFACE)){
                String ipv4 = HttpUtil.get(parsingRecord.getGetIpModeValue().trim());
                parsingRecord.setIp(ipv4);
                return ipv4.trim();
            }else if(parsingRecord.getGetIpMode().equals(IP_MODE_NETWORK)){
                parsingRecord.setIp(parsingRecord.getGetIpModeValue());
                return parsingRecord.getGetIpModeValue().trim();
            }
        }
        return null;
    }

    @Resource
    private NetWorkService netWorkService;

    @Override
    public List<NetWorkSelectResponse> getModeIpValue(Integer getIpMode,Integer recordType) throws SocketException {
        if(IP_MODE_INTERFACE.equals(getIpMode)){
            if (RECORD_TYPE_AAAA.equals(recordType)) {
                return Arrays.stream(IPV6_INTERFACE_VALUES).map(item->{
                    NetWorkSelectResponse netWorkSelectResponse = new NetWorkSelectResponse();
                    netWorkSelectResponse.setLabel(item);
                    netWorkSelectResponse.setValue(item);
                    return netWorkSelectResponse;
                }).collect(Collectors.toList());
            }else if(RECORD_TYPE_A.equals(recordType)){
                return Arrays.stream(IPV4_INTERFACE_VALUES).map(item->{
                    NetWorkSelectResponse netWorkSelectResponse = new NetWorkSelectResponse();
                    netWorkSelectResponse.setLabel(item);
                    netWorkSelectResponse.setValue(item);
                    return netWorkSelectResponse;
                }).collect(Collectors.toList());
            }
        }else if(IP_MODE_NETWORK.equals(getIpMode)){
            return netWorkService.networks(recordType);
        }
        return null;
    }

    @Override
    public AmisPageUtils<ParsingRecord> queryPage(ParsingRecord parsingRecord) {
        Page<ParsingRecord> pageList = lambdaQuery()
                .eq(Objects.nonNull(parsingRecord.getServiceProvider()), ParsingRecord::getServiceProvider, parsingRecord.getServiceProvider())
                .eq(StringUtils.hasText(parsingRecord.getDomain()), ParsingRecord::getDomain, parsingRecord.getDomain())
                .eq(Objects.nonNull(parsingRecord.getRecordType()), ParsingRecord::getRecordType, parsingRecord.getRecordType())
                .eq(Objects.nonNull(parsingRecord.getState()), ParsingRecord::getState, parsingRecord.getState())
                .ge(Objects.nonNull(parsingRecord.getCreateDate()), ParsingRecord::getCreateDate, parsingRecord.getCreateDate())
                .le(Objects.nonNull(parsingRecord.getUpdateDate()), ParsingRecord::getUpdateDate, parsingRecord.getUpdateDate())
                .page(new Page<ParsingRecord>(parsingRecord.getPage(), parsingRecord.getPerPage()));

        List<ParsingRecord> resultList = pageList.getRecords().stream().map(item -> {
            String serviceProviderName = ServiceProviderEnum.getNameByIndex(item.getServiceProvider());
            String recordTypeName = RecordTypeEnum.getNameByIndex(item.getRecordType());
            item.setServiceProviderName(serviceProviderName);
            item.setRecordTypeName(recordTypeName);
            return item;
        }).collect(Collectors.toList());
        pageList.setRecords(resultList);

        return new AmisPageUtils<>(pageList);
    }
}
