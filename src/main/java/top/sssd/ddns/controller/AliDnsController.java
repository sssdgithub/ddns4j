package top.sssd.ddns.controller;

import com.aliyun.alidns20150109.models.DescribeDomainRecordsResponseBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.sssd.ddns.service.AliDnsService;

import java.util.List;

/**
 *  FIXME: 2023/3/19 接口重新设计完成后将被废弃
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
