package com.sssd.alidns.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author sssd
 * @update 2022-12-18 17:04
 */
@Component
@Data
public class AliyunConfig {
    @Value("${aliyun.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.accessKeySecret}")
    private  String accessKeySecret;

    @Value("${aliyun.domainName}")
    private  String domainName;

    @Value("${aliyun.RR}")
    private  String rr;

    @Value("${aliyun.recordType}")
    private  String recordType;
    
}
