package top.sssd.ddns.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.sssd.ddns.common.BizException;
import top.sssd.ddns.common.enums.RecordTypeEnum;
import top.sssd.ddns.model.entity.ParsingRecord;
import top.sssd.ddns.service.DynamicDnsService;
import top.sssd.ddns.utils.CloudflareUtils;

/**
 * @author sssd
 * @created 2023-05-06-17:13
 */
@Service
@Slf4j
public class CloudflareDynamicDnsServiceImpl implements DynamicDnsService {

    @Override
    public boolean exist(String serviceProviderId, String serviceProviderSecret, String subDomain, String recordType) {
        CloudflareUtils.CloudflareQueryResponse cloudflareResponse = CloudflareUtils.getSubDomainParseList(serviceProviderId, serviceProviderSecret, subDomain, recordType);
        return cloudflareResponse.getResult().size() > 0;
    }

    @Override
    public void add(ParsingRecord parsingRecord, String ip) throws JsonProcessingException {
        CloudflareUtils.CloudflareResponse cloudflareResponse =
                CloudflareUtils.add(parsingRecord.getServiceProviderId(), parsingRecord.getServiceProviderSecret(), parsingRecord.getDomain(), RecordTypeEnum.getNameByIndex(parsingRecord.getRecordType()), ip);
        if (Boolean.FALSE.equals(cloudflareResponse.getSuccess())) {
            log.error("域名添加失败:{}", parsingRecord);
            throw new BizException("域名添加失败");
        }

    }

    @Override
    public void update(ParsingRecord parsingRecord, String ip, String recordId) {
        CloudflareUtils.CloudflareResponse cloudflareResponse = CloudflareUtils.update(parsingRecord.getServiceProviderId(),
                parsingRecord.getServiceProviderSecret(),
                parsingRecord.getDomain(),
                RecordTypeEnum.getNameByIndex(parsingRecord.getRecordType()), ip,recordId);
        if (Boolean.FALSE.equals(cloudflareResponse.getSuccess())) {
            log.error("域名更新失败:{}", parsingRecord);
            throw new BizException("域名更新失败");
        }
    }

    @Override
    public String getRecordId(ParsingRecord parsingRecord, String ip) {
        return CloudflareUtils
                .getId(parsingRecord.getServiceProviderId()
                        , parsingRecord.getServiceProviderSecret()
                        , parsingRecord.getDomain()
                        , RecordTypeEnum.getNameByIndex(parsingRecord.getRecordType()), ip);
    }

    @Override
    public void remove(ParsingRecord parsingRecord, String ip) {
        CloudflareUtils.CloudflareResponse cloudflareResponse = CloudflareUtils.delete(parsingRecord.getServiceProviderId()
                , parsingRecord.getServiceProviderSecret()
                , parsingRecord.getDomain()
                , RecordTypeEnum.getNameByIndex(parsingRecord.getRecordType()), ip);
        if (Boolean.FALSE.equals(cloudflareResponse.getSuccess())) {
            log.error("域名删除失败:{}", parsingRecord);
            throw new BizException("域名删除失败");
        }
    }

    @Override
    public String getIpBySubDomainWithType(ParsingRecord parsingRecord) {
        return CloudflareUtils.getIpBySubDomainWithType(parsingRecord.getServiceProviderId()
                , parsingRecord.getServiceProviderSecret()
                , parsingRecord.getDomain()
                , RecordTypeEnum.getNameByIndex(parsingRecord.getRecordType()));
    }
}
