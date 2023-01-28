package com.sssd.alidns.service;

import com.aliyun.alidns20150109.models.DescribeDomainRecordsResponseBody;

import java.util.List;

/**
 * @author sssd
 * @update 2022-12-18 17:13
 */
public interface AliDnsService {

    String update(String ipv6) throws Exception;

    List<DescribeDomainRecordsResponseBody.DescribeDomainRecordsResponseBodyDomainRecordsRecord> list() throws Exception;
}
