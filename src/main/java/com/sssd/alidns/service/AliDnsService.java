package com.sssd.alidns.service;

import com.aliyun.alidns20150109.models.DescribeDomainRecordsResponseBody;

import java.util.List;

/**
 * @author sssd
 * @update 2022-12-18 17:13
 */
public interface AliDnsService {

    /**
     * 更新解析ipv6地址
     * @param ipv6
     * @return
     * @throws Exception
     */
    String update(String ipv6);

    /**
     * 查看配置域名的解析列表
     * @return
     * @throws Exception
     */
    List<DescribeDomainRecordsResponseBody.DescribeDomainRecordsResponseBodyDomainRecordsRecord> list() ;
}
