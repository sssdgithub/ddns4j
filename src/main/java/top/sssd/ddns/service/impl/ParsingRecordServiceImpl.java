package top.sssd.ddns.service.impl;

import cn.hutool.http.HttpUtil;
import com.aliyun.alidns20150109.Client;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.sssd.ddns.common.BizException;
import top.sssd.ddns.common.enums.RecordTypeEnum;
import top.sssd.ddns.common.enums.ServiceProviderEnum;
import top.sssd.ddns.common.enums.UpdateFrequencyEnum;
import top.sssd.ddns.common.utils.PageUtils;
import top.sssd.ddns.mapper.ParsingRecordMapper;
import top.sssd.ddns.model.entity.JobTask;
import top.sssd.ddns.model.entity.ParsingRecord;
import top.sssd.ddns.service.DynamicDnsService;
import top.sssd.ddns.service.IJobTaskService;
import top.sssd.ddns.service.IParsingRecordService;
import top.sssd.ddns.task.DynamicDnsJob;
import top.sssd.ddns.utils.AliDnsUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private DynamicDnsService dynamicDnsService;
    @Resource
    private IJobTaskService jobTaskService;

    @Override
    public void add(ParsingRecord parsingRecord) {
        // TODO: 2023/5/4 后端唯一性校验
        String ip = getIp(parsingRecord);
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
    public void modify(ParsingRecord parsingRecord) {
        // TODO: 2023/5/4 后端唯一性校验
        ParsingRecord dbParsingRecord = this.getById(parsingRecord.getId());
        if (Objects.isNull(dbParsingRecord)) {
            throw new BizException("该记录不存在");
        }
//        if (dynamicDnsService.exist(parsingRecord.getServiceProviderId(),
//                parsingRecord.getServiceProviderSecret(),
//                parsingRecord.getDomain(),
//                RecordTypeEnum.getNameByIndex(parsingRecord.getRecordType()))) {
//            throw new BizException("该记录已在域名服务商中存在");
//        }

        // 删除之前的定时任务
        JobTask one = jobTaskService.lambdaQuery().eq(JobTask::getName, dbParsingRecord.getId().toString()).one();
        if (Objects.nonNull(one)) {
            jobTaskService.deleteJobTask(one.getId());
        }

        if(parsingRecord.getState().equals(0)){
            this.updateById(parsingRecord);
            return;
        }

        Client client = AliDnsUtils.createClient(dbParsingRecord.getServiceProviderId(), dbParsingRecord.getServiceProviderSecret());
        String dnsIp = AliDnsUtils.getIpBySubDomainWithType(client, dbParsingRecord.getDomain(), RecordTypeEnum.getNameByIndex(dbParsingRecord.getRecordType()));
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
    public void delete(Long id) {
        ParsingRecord parsingRecord = this.getById(id);
        if (Objects.isNull(parsingRecord)) {
            throw new BizException("该记录不存在");
        }
        if (!dynamicDnsService.exist(parsingRecord.getServiceProviderId(),
                parsingRecord.getServiceProviderSecret(),
                parsingRecord.getDomain(),
                RecordTypeEnum.getNameByIndex(parsingRecord.getRecordType()))) {
            throw new BizException("该记录在域名服务商中不存在");
        }
        // FIXME: 2023/5/4 这可能导致数据不同步的问题
        String ip = getIp(parsingRecord);
        dynamicDnsService.remove(parsingRecord, ip);
        this.removeById(id);
        //  2023/5/2 删除定时任务
        JobTask one = jobTaskService.lambdaQuery().eq(JobTask::getName, parsingRecord.getId()).one();
        if (Objects.nonNull(one)) {
            jobTaskService.deleteJobTask(one.getId());
        }
    }

    @Override
    public PageUtils queryPage(ParsingRecord parsingRecord) {
        Page<ParsingRecord> page = lambdaQuery()
                .eq(Objects.nonNull(parsingRecord.getServiceProvider()),ParsingRecord::getServiceProvider,parsingRecord.getServiceProvider())
                .eq(StringUtils.hasText(parsingRecord.getDomain()), ParsingRecord::getDomain, parsingRecord.getDomain())
                .eq(Objects.nonNull(parsingRecord.getRecordType()), ParsingRecord::getRecordType, parsingRecord.getRecordType())
                .eq(Objects.nonNull(parsingRecord.getState()), ParsingRecord::getState, parsingRecord.getState())
                .ge(Objects.nonNull(parsingRecord.getCreateDate()), ParsingRecord::getCreateDate, parsingRecord.getCreateDate())
                .le(Objects.nonNull(parsingRecord.getUpdateDate()), ParsingRecord::getUpdateDate, parsingRecord.getUpdateDate())
                .page(new Page<ParsingRecord>(parsingRecord.getPage(), parsingRecord.getPageSize()));
        List<ParsingRecord> records = page.getRecords();
        records = records.stream().map(item -> {
            String serviceProviderName = ServiceProviderEnum.getNameByIndex(item.getServiceProvider());
            String recordTypeName = RecordTypeEnum.getNameByIndex(item.getRecordType());
            item.setServiceProviderName(serviceProviderName);
            item.setRecordTypeName(recordTypeName);
            return item;
        }).collect(Collectors.toList());
        return new PageUtils(page);
    }

    @Override
    public String getIp(ParsingRecord parsingRecord) {
        //解析类型:1 AAAA 2 A
        Integer getIpMode = parsingRecord.getGetIpMode();
        //获取ip方式: 1 interface 2 network 3 cmd
        String getIpModeValue = parsingRecord.getGetIpModeValue();
        // TODO: 2023/5/2 目前只支持 interface
        if (getIpMode.equals(1)) {
            //ipv6
            List<String> ipInterfaces =
                    Arrays.asList("https://v6.ip.zxinc.org/getip", "https://api6.ipify.org", "https://api.ip.sb/ip", "https://api.myip.la");
            String ipv6Interface = ipInterfaces.stream().findAny().orElseGet(null);
            parsingRecord.setGetIpModeValue(ipv6Interface);
            parsingRecord.setRecordType(1);
            String ipv6 = HttpUtil.get(ipv6Interface);
            parsingRecord.setIp(ipv6);
            return ipv6;
        } else if (getIpMode.equals(2)) {
            //ipv4
            List<String> ipInterfaces =
                    Arrays.asList("https://ip.3322.net", "https://4.ipw.cn");
            String ipv4Interface = ipInterfaces.stream().findAny().orElseGet(null);
            parsingRecord.setGetIpModeValue(ipv4Interface);
            parsingRecord.setRecordType(2);
            String ipv4 = HttpUtil.get(ipv4Interface);
            parsingRecord.setIp(ipv4);
            return ipv4;
        }
        return null;
    }
}
