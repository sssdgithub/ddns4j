package top.sssd.ddns.service.impl;

import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import top.sssd.ddns.common.BizException;
import top.sssd.ddns.common.enums.RecordTypeEnum;
import top.sssd.ddns.common.enums.ServiceProviderEnum;
import top.sssd.ddns.common.enums.UpdateFrequencyEnum;
import top.sssd.ddns.common.utils.AmisPageUtils;
import top.sssd.ddns.mapper.ParsingRecordMapper;
import top.sssd.ddns.model.entity.JobTask;
import top.sssd.ddns.model.entity.ParsingRecord;
import top.sssd.ddns.model.response.NetWorkSelectResponse;
import top.sssd.ddns.strategy.DynamicDnsStrategy;
import top.sssd.ddns.service.IJobTaskService;
import top.sssd.ddns.service.IParsingRecordService;
import top.sssd.ddns.service.NetWorkService;
import top.sssd.ddns.strategy.DynamicDnsServiceFactory;
import top.sssd.ddns.task.DynamicDnsJob;

import javax.annotation.Resource;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Collections;
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

    @Resource
    private DefaultIdentifierGenerator defaultIdentifierGenerator;

    @Resource
    private DynamicDnsServiceFactory dnsServiceFactory;

    @Override
    public void add(ParsingRecord parsingRecord) throws Exception {
        DynamicDnsStrategy dynamicDnsService = dnsServiceFactory.getServiceInstance(parsingRecord.getServiceProvider());

        String ip = getIp(parsingRecord);

        //后端唯一性校验
        ParsingRecord checkParsingRecord = this.lambdaQuery()
                .eq(ParsingRecord::getServiceProvider, parsingRecord.getServiceProvider())
                .eq(ParsingRecord::getRecordType, parsingRecord.getRecordType())
                .eq(ParsingRecord::getDomain, parsingRecord.getDomain())
                .eq(ParsingRecord::getIp, parsingRecord.getIp())
                .last("limit 1").one();
        if (Objects.nonNull(checkParsingRecord)) {
            throw new BizException("同一域名,同一解析类型,同一ip,不能重复添加");
        }
        if (dynamicDnsService.exist(parsingRecord.getServiceProviderId(),
                parsingRecord.getServiceProviderSecret(),
                parsingRecord.getDomain(),
                RecordTypeEnum.getNameByIndex(parsingRecord.getRecordType()))) {
            throw new BizException("该记录已在域名服务商中存在");
        }
        this.save(parsingRecord);
        dynamicDnsService.add(parsingRecord, ip);
        //添加并启动一个定时任务
        addWithStartTask(parsingRecord);
    }

    @Override
    public void copy(ParsingRecord parsingRecord) throws Exception {
        long newId = defaultIdentifierGenerator.nextId(parsingRecord).longValue();
        parsingRecord.setId(newId);
        add(parsingRecord);
    }

    @Override
    public void modify(ParsingRecord parsingRecord) throws Exception {
        DynamicDnsStrategy dynamicDnsService = dnsServiceFactory.getServiceInstance(parsingRecord.getServiceProvider());

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
            throw new BizException("同一域名,同一解析类型,同一ip,不允许重复更新");
        }

        String dnsIp = null;
        dnsIp = dynamicDnsService.getIpBySubDomainWithType(dbParsingRecord);
        String recordId = dynamicDnsService.getRecordId(dbParsingRecord, dnsIp);
        String ip = getIp(parsingRecord);
        //是否修改了服务商相关信息
        if(updatedServiceProvider(dbParsingRecord,parsingRecord)||!ip.equals(dnsIp)){
            dynamicDnsService.update(parsingRecord, ip, recordId);
        }
        this.updateById(parsingRecord);
        // 删除之前的定时任务
        JobTask one = jobTaskService.lambdaQuery().eq(JobTask::getName, dbParsingRecord.getId().toString()).one();
        if (Objects.nonNull(one)) {
            jobTaskService.deleteJobTask(one.getId());
        }
        // 添加并启动一个定时任务
        addWithStartTask(parsingRecord);
    }

    //是否修改了服务商相关信息
    public boolean updatedServiceProvider(ParsingRecord dbParsingRecord, ParsingRecord parsingRecord) {
        return !dbParsingRecord.getServiceProvider().equals(parsingRecord.getServiceProvider()) ||
                !dbParsingRecord.getServiceProviderId().equals(parsingRecord.getServiceProviderId()) ||
                !dbParsingRecord.getServiceProviderSecret().equals(parsingRecord.getServiceProviderSecret()) ||
                !dbParsingRecord.getRecordType().equals(parsingRecord.getRecordType()) ||
                !dbParsingRecord.getDomain().equals(parsingRecord.getDomain());
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
        DynamicDnsStrategy dynamicDnsService = dnsServiceFactory.getServiceInstance(parsingRecord.getServiceProvider());

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

    @Resource
    private RestTemplate restTemplate;

    @Override
    public String getIp(ParsingRecord parsingRecord) {
        Integer recordType = parsingRecord.getRecordType();
        Integer getIpMode = parsingRecord.getGetIpMode();
        String getIpModeValue = parsingRecord.getGetIpModeValue().trim();

        if (recordType.equals(RECORD_TYPE_AAAA)) {
            // AAAA record type (ipv6)
            return handleIpv6(getIpMode, getIpModeValue, parsingRecord);
        } else if (recordType.equals(RECORD_TYPE_A)) {
            // A record type (ipv4)
            return handleIpv4(getIpMode, getIpModeValue, parsingRecord);
        }else{
            throw new BizException("参数错误-recordType:"+recordType);
        }
    }

    private String handleIpv6(Integer getIpMode, String getIpModeValue, ParsingRecord parsingRecord) {
        if (IP_MODE_INTERFACE.equals(getIpMode)) {
            String ipv6 = restTemplate.getForObject(getIpModeValue, String.class);
            if (!StringUtils.hasText(ipv6)) {
                throw new BizException("通过网络接口获取ipv6地址失败，请检查网卡是否分配ipv6地址");
            }
            parsingRecord.setIp(ipv6);
            return ipv6.trim();
        } else if (IP_MODE_NETWORK.equals(getIpMode)) {
            if (!StringUtils.hasText(getIpModeValue)) {
                throw new BizException("通过本地网卡获取ipv6地址失败，请检查网卡是否分配ipv6地址");
            }
            parsingRecord.setIp(getIpModeValue);
            return getIpModeValue;
        }else{
            throw new BizException("参数错误-getIpMode:"+getIpMode);
        }
    }

    private String handleIpv4(Integer getIpMode, String getIpModeValue, ParsingRecord parsingRecord) {
        if (IP_MODE_INTERFACE.equals(getIpMode)) {
            String ipv4 = restTemplate.getForObject(getIpModeValue, String.class);
            if (!StringUtils.hasText(ipv4)) {
                throw new BizException("通过网络接口获取ipv4地址失败，请检查网卡是否分配ipv4地址");
            }
            parsingRecord.setIp(ipv4);
            return ipv4.trim();
        } else if (IP_MODE_NETWORK.equals(getIpMode)) {
            parsingRecord.setIp(getIpModeValue);
            return getIpModeValue;
        }else{
            throw new BizException("参数错误-getIpMode:"+getIpMode);
        }
    }

    @Resource
    private NetWorkService netWorkService;

    @Override
    public List<NetWorkSelectResponse> getModeIpValue(Integer getIpMode, Integer recordType) throws SocketException {
        if (IP_MODE_INTERFACE.equals(getIpMode)) {
            if (RECORD_TYPE_AAAA.equals(recordType)) {
                return Arrays.stream(IPV6_INTERFACE_VALUES).map(item -> {
                    NetWorkSelectResponse netWorkSelectResponse = new NetWorkSelectResponse();
                    netWorkSelectResponse.setLabel(item);
                    netWorkSelectResponse.setValue(item);
                    return netWorkSelectResponse;
                }).collect(Collectors.toList());
            } else if (RECORD_TYPE_A.equals(recordType)) {
                return Arrays.stream(IPV4_INTERFACE_VALUES).map(item -> {
                    NetWorkSelectResponse netWorkSelectResponse = new NetWorkSelectResponse();
                    netWorkSelectResponse.setLabel(item);
                    netWorkSelectResponse.setValue(item);
                    return netWorkSelectResponse;
                }).collect(Collectors.toList());
            }
        } else if (IP_MODE_NETWORK.equals(getIpMode)) {
            return netWorkService.networks(recordType);
        }
        return Collections.emptyList();
    }

    @Override
    public AmisPageUtils<ParsingRecord> queryPage(ParsingRecord parsingRecord) {
        Page<ParsingRecord> pageList = lambdaQuery()
                .eq(Objects.nonNull(parsingRecord.getServiceProvider()), ParsingRecord::getServiceProvider, parsingRecord.getServiceProvider())
                .eq(StringUtils.hasText(parsingRecord.getDomain()), ParsingRecord::getDomain, parsingRecord.getDomain())
                .eq(Objects.nonNull(parsingRecord.getRecordType()), ParsingRecord::getRecordType, parsingRecord.getRecordType())
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
