package top.sssd.ddns.service.impl;

import top.sssd.ddns.model.entity.ParsingRecord;
import top.sssd.ddns.service.DynamicDnsService;

/**
 * @author sssd
 * @careate 2023-05-06-17:13
 */
public class CloudflareDynamicDnsServiceImpl implements DynamicDnsService {
    @Override
    public boolean exist(String serviceProviderId, String serviceProviderSecret, String subDomain, String recordType) throws Exception {
        return false;
    }

    @Override
    public void add(ParsingRecord parsingRecord, String ip) {

    }

    @Override
    public void update(ParsingRecord parsingRecord, String ip, String recordId) throws Exception {

    }

    @Override
    public String getRecordId(ParsingRecord parsingRecord, String ip) throws Exception {
        return null;
    }

    @Override
    public void remove(ParsingRecord parsingRecord, String ip) throws Exception {

    }

    @Override
    public String getIpBySubDomainWithType(ParsingRecord parsingRecord) {
        return null;
    }
}
