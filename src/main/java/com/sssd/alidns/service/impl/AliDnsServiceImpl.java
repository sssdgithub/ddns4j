package com.sssd.alidns.service.impl;

import com.aliyun.alidns20150109.models.DescribeDomainRecordsResponse;
import com.aliyun.alidns20150109.models.DescribeDomainRecordsResponseBody;
import com.aliyun.alidns20150109.models.UpdateDomainRecordRequest;
import com.sssd.alidns.config.AliyunConfig;
import com.sssd.alidns.service.AliDnsService;
import com.sssd.alidns.utils.AliDnsUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * @author sssd
 * @update 2022-12-18 17:15
 */
@Service
@Slf4j
public class AliDnsServiceImpl implements AliDnsService {

    @Autowired
    private AliyunConfig aliyunConfig;

    @Override
    public String update(String ipv6) throws Exception {
        List<DescribeDomainRecordsResponseBody.DescribeDomainRecordsResponseBodyDomainRecordsRecord> list = list();
        if (CollectionUtils.isEmpty(list)) {
            log.error("解析列表为空!");
            return "解析列表为空!";
        }
        for (DescribeDomainRecordsResponseBody.DescribeDomainRecordsResponseBodyDomainRecordsRecord record : list) {
            if (ipv6.equals(record.value)) {
                log.info("该ipv6已被解析");
                return "该ipv6已被解析";
            }
        }
        DescribeDomainRecordsResponseBody.DescribeDomainRecordsResponseBodyDomainRecordsRecord record = list.get(0);
        //修改解析记录
        UpdateDomainRecordRequest req = new UpdateDomainRecordRequest();
        // 主机记录
        req.RR = record.RR;
        // 记录ID
        req.recordId = record.recordId;
        // 将主机记录值改为当前主机IP
        req.value = ipv6;
        // 解析记录类型
        req.type = record.type;
        AliDnsUtils.updateDomainRecord(createClient(), req);
        return "解析成功,请在解析列表页面查看对应记录!";
    }

    @Override
    public List<DescribeDomainRecordsResponseBody.DescribeDomainRecordsResponseBodyDomainRecordsRecord> list() throws Exception {
        DescribeDomainRecordsResponse resp = AliDnsUtils.getParseList(createClient(), aliyunConfig.getDomainName(), aliyunConfig.getRr(), aliyunConfig.getRecordType());
        List<DescribeDomainRecordsResponseBody.DescribeDomainRecordsResponseBodyDomainRecordsRecord> records = resp.body.domainRecords.record;
        if (Objects.isNull(resp) || CollectionUtils.isEmpty(records)) {
            log.error("参数错误");
            return null;
        }
        return records;
    }

    public com.aliyun.alidns20150109.Client createClient() throws Exception {
        return AliDnsUtils.createClient(aliyunConfig.getAccessKeyId(), aliyunConfig.getAccessKeySecret());
    }
}
