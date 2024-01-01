package top.sssd.ddns.service.impl;


import org.springframework.stereotype.Service;
import top.sssd.ddns.common.BizException;
import top.sssd.ddns.common.enums.RecordTypeEnum;
import top.sssd.ddns.model.entity.ParsingRecord;
import top.sssd.ddns.service.DynamicDnsService;
import top.sssd.ddns.utils.TencentDnsUtils;

import java.util.Objects;

import static top.sssd.ddns.common.utils.DoMainUtil.spiltDomain;

/**
 * @author sssd
 * @created 2023-05-06-17:11
 */
@Service
public class TencentDynamicDnsServiceImpl implements DynamicDnsService {

    @Override
    public boolean exist(String serviceProviderId, String serviceProviderSecret, String domain, String recordType) throws Exception {
        String[] domains = spiltDomain(domain);
        TencentDnsUtils.ListRecordResponse listRecordResponse = TencentDnsUtils.getRecordList(domains[0], domains[1], recordType, serviceProviderId, serviceProviderSecret);

        if(Objects.isNull(listRecordResponse)){
            throw new BizException("TencentCloud 查询列表记录失败 没有来自腾讯云的响应");
        }
        TencentDnsUtils.ListResponse listResponse = listRecordResponse.getResponse();
        if(Objects.isNull(listResponse)){
            throw new BizException("TencentCloud 查询列表记录失败 腾讯云的列表记录响应对象为空");
        }
        return !(listResponse.getRecordList().isEmpty());
    }

    @Override
    public void add(ParsingRecord parsingRecord, String ip) throws Exception {
        String domain = parsingRecord.getDomain();
        String[] domains = spiltDomain(domain);
        TencentDnsUtils.CreateRecordResponse recordResponse = TencentDnsUtils.createRecord(domains[0], domains[1], RecordTypeEnum.getNameByIndex(parsingRecord.getRecordType()), parsingRecord.getServiceProviderId(), parsingRecord.getServiceProviderSecret(), ip);
        if(Objects.isNull(recordResponse)){
            throw new BizException("TencentCloud 添加记录失败 没有来自腾讯云的响应");
        }
        TencentDnsUtils.CreateResponse createResponse = recordResponse.getResponse();
        if(Objects.isNull(createResponse)){
            throw new BizException("TencentCloud 添加记录失败 腾讯云的添加响应对象为空");
        }
        TencentDnsUtils.Error error = createResponse.getError();
        if(Objects.nonNull(error)){
            throw new BizException("TencentCloud 添加记录失败"+error.getMessage());
        }
    }

    @Override
    public void update(ParsingRecord parsingRecord, String ip, String recordId) throws Exception {
        String domain = parsingRecord.getDomain();
        String[] domains = spiltDomain(domain);
        TencentDnsUtils.UpdateRecordResponse updateRecordResponse = TencentDnsUtils.updateRecord(domains[0], domains[1], RecordTypeEnum.getNameByIndex(parsingRecord.getRecordType()), parsingRecord.getServiceProviderId(), parsingRecord.getServiceProviderSecret(), ip, Integer.parseInt(recordId));
        if(Objects.isNull(updateRecordResponse)){
            throw new BizException("TencentCloud 更新记录失败 没有来自腾讯云的响应");
        }
        TencentDnsUtils.UpdateResponse updateResponse = updateRecordResponse.getResponse();
        if(Objects.isNull(updateResponse)){
            throw new BizException("TencentCloud 更新记录失败 腾讯云的更新响应对象为空");
        }
        TencentDnsUtils.Error error = updateResponse.getError();
        if(Objects.nonNull(error)){
            throw new BizException("TencentCloud 更新记录失败"+error.getMessage());
        }
    }

    @Override
    public String getRecordId(ParsingRecord parsingRecord, String ip) throws Exception {
        String domain = parsingRecord.getDomain();
        String[] domains = spiltDomain(domain);
        return  TencentDnsUtils.getRecordId(domains[0], domains[1], RecordTypeEnum.getNameByIndex(parsingRecord.getRecordType()), parsingRecord.getServiceProviderId(), parsingRecord.getServiceProviderSecret());
    }

    @Override
    public void remove(ParsingRecord parsingRecord, String ip) throws Exception {
        String domain = parsingRecord.getDomain();
        String resultDomain = domain.substring(domain.indexOf('.') + 1);
        String recordId = getRecordId(parsingRecord, ip);
        TencentDnsUtils.DeleteRecordResponse deleteRecordResponse = TencentDnsUtils.deleteRecord(resultDomain, parsingRecord.getServiceProviderId(), parsingRecord.getServiceProviderSecret(), Integer.parseInt(recordId));
        if(Objects.isNull(deleteRecordResponse)){
            throw new BizException("TencentCloud 删除记录失败 没有来自腾讯云的响应");
        }
        TencentDnsUtils.DeleteResponse deleteResponse = deleteRecordResponse.getResponse();
        if(Objects.isNull(deleteResponse)){
            throw new BizException("TencentCloud 删除记录失败 腾讯云的删除响应对象为空");
        }
        TencentDnsUtils.Error error = deleteResponse.getError();
        if(Objects.nonNull(error)){
            throw new BizException("TencentCloud 删除记录失败"+error.getMessage());
        }
    }

    @Override
    public String getIpBySubDomainWithType(ParsingRecord parsingRecord) throws Exception {
        String domain = parsingRecord.getDomain();
        String[] domains = spiltDomain(domain);
        return TencentDnsUtils.getIpBySubDomainWithType(domains[0], domains[1], RecordTypeEnum.getNameByIndex(parsingRecord.getRecordType()), parsingRecord.getServiceProviderId(), parsingRecord.getServiceProviderSecret());
    }
}
