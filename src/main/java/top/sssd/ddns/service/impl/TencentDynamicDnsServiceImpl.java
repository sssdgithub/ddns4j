package top.sssd.ddns.service.impl;


import org.springframework.stereotype.Service;
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
        TencentDnsUtils.ListRecordResponse recordList = TencentDnsUtils.getRecordList(domains[0], domains[1], recordType, serviceProviderId, serviceProviderSecret);
        return Objects.nonNull(recordList) && recordList.getResponse().getRecordList().size() > 0;
    }

    @Override
    public void add(ParsingRecord parsingRecord, String ip) throws Exception {
        String domain = parsingRecord.getDomain();
        String[] domains = spiltDomain(domain);
        TencentDnsUtils.createRecord(domains[0], domains[1], RecordTypeEnum.getNameByIndex(parsingRecord.getRecordType()), parsingRecord.getServiceProviderId(), parsingRecord.getServiceProviderSecret(), ip);
    }

    @Override
    public void update(ParsingRecord parsingRecord, String ip, String recordId) throws Exception {
        String domain = parsingRecord.getDomain();
        String[] domains = spiltDomain(domain);
        TencentDnsUtils.updateRecord(domains[0], domains[1], RecordTypeEnum.getNameByIndex(parsingRecord.getRecordType()), parsingRecord.getServiceProviderId(), parsingRecord.getServiceProviderSecret(), ip, Integer.parseInt(recordId));
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
        TencentDnsUtils.deleteRecord(resultDomain, parsingRecord.getServiceProviderId(), parsingRecord.getServiceProviderSecret(), Integer.parseInt(recordId));
    }

    @Override
    public String getIpBySubDomainWithType(ParsingRecord parsingRecord) throws Exception {
        String domain = parsingRecord.getDomain();
        String[] domains = spiltDomain(domain);
        return TencentDnsUtils.getIpBySubDomainWithType(domains[0], domains[1], RecordTypeEnum.getNameByIndex(parsingRecord.getRecordType()), parsingRecord.getServiceProviderId(), parsingRecord.getServiceProviderSecret());
    }
}
