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
    public String update(String ipv6)  {
        List<DescribeDomainRecordsResponseBody.DescribeDomainRecordsResponseBodyDomainRecordsRecord> list = list();
        if (CollectionUtils.isEmpty(list)) {
            log.error("解析列表为空!");
            return "解析列表为空!";
        }
        for (DescribeDomainRecordsResponseBody.DescribeDomainRecordsResponseBodyDomainRecordsRecord rd : list) {
            if (ipv6.equals(rd.value)) {
                log.info("该ipv6已被解析");
                return "该ipv6已被解析";
            }
        }
        DescribeDomainRecordsResponseBody.DescribeDomainRecordsResponseBodyDomainRecordsRecord rd = null;
        rd = list.get(0);
        //修改解析记录
        UpdateDomainRecordRequest req = new UpdateDomainRecordRequest();
        // 主机记录
        req.RR = rd.RR;
        // 记录ID
        req.recordId = rd.recordId;
        // 将主机记录值改为当前主机IP
        req.value = ipv6;
        // 解析记录类型
        req.type = rd.type;
        try {
            AliDnsUtils.updateDomainRecord(createClient(), req);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "解析成功,请在解析列表页面查看对应记录!";
    }

    @Override
    public List<DescribeDomainRecordsResponseBody.DescribeDomainRecordsResponseBodyDomainRecordsRecord> list()  {
        DescribeDomainRecordsResponse resp = null;
        try {
            resp = AliDnsUtils.getParseList(createClient(), aliyunConfig.getDomainName(), aliyunConfig.getRr(), aliyunConfig.getRecordType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Objects.isNull(resp)) {
            log.error("参数错误: resp");
            return null;
        }
        List<DescribeDomainRecordsResponseBody.DescribeDomainRecordsResponseBodyDomainRecordsRecord> records = resp.body.domainRecords.record;
        if (CollectionUtils.isEmpty(records)) {
            log.error("参数错误");
            return null;
        }
        return records;
    }

    public com.aliyun.alidns20150109.Client createClient() throws Exception {
        return AliDnsUtils.createClient(aliyunConfig.getAccessKeyId(), aliyunConfig.getAccessKeySecret());
    }
}
