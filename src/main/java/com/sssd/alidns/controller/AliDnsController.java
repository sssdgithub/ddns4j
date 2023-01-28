package com.sssd.alidns.controller;

import com.aliyun.alidns20150109.models.DescribeDomainRecordsResponseBody;
import com.sssd.alidns.service.AliDnsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author sssd
 * @update 2022-12-18 17:09
 */
@RestController
@RequestMapping("alidns")
@Slf4j
public class AliDnsController {


    @Autowired
    private AliDnsService aliDnsService;


    @PostMapping ("update")
    public String update(String ipv6) throws Exception {
        return aliDnsService.update(ipv6);
    }

    @GetMapping("list")
    public List<DescribeDomainRecordsResponseBody.DescribeDomainRecordsResponseBodyDomainRecordsRecord> list() throws Exception {
        return aliDnsService.list();
    }
}
