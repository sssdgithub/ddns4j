package com.sssd.alidns.utils;

import com.aliyun.alidns20150109.models.DescribeDomainRecordsRequest;
import com.aliyun.alidns20150109.models.DescribeDomainRecordsResponse;
import com.aliyun.alidns20150109.models.UpdateDomainRecordRequest;
import com.aliyun.alidns20150109.models.UpdateDomainRecordResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.tea.TeaModel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author sssd
 * @update 2022-12-17 23:40
 */
@Slf4j
public class AliDnsUtils {

    private AliDnsUtils() {
    }

    /**
     * 使用AK&SK初始化账号Client
     *
     * @param accessKeyId
     * @param accessKeySecret
     * @return Client
     * @throws Exception
     */
    public static com.aliyun.alidns20150109.Client createClient(String accessKeyId, String accessKeySecret) {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                // 必填，您的 AccessKey ID
                .setAccessKeyId(accessKeyId)
                // 必填，您的 AccessKey Secret
                .setAccessKeySecret(accessKeySecret);
        // 访问的域名
        config.endpoint = "alidns.cn-hangzhou.aliyuncs.com";
        try {
            return new com.aliyun.alidns20150109.Client(config);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取主域名的所有解析记录列表
     */
    public static DescribeDomainRecordsResponse getParseList(com.aliyun.alidns20150109.Client client, String domainName, String RR, String recordType) throws Exception {
        DescribeDomainRecordsRequest req = new DescribeDomainRecordsRequest();
        // 主域名 一级 域名 jjjr.site
        req.domainName = domainName;
        // 主机记录 ?.jjjr.site  ? > RR
        req.RRKeyWord = RR;
        // 解析记录类型 type > AAAA
        req.type = recordType;
        try {
            DescribeDomainRecordsResponse resp = client.describeDomainRecords(req);
            log.info("-------------------获取主域名的所有解析记录列表--------------------");
            log.info(com.aliyun.teautil.Common.toJSONString(TeaModel.buildMap(resp)));
            return resp;
        } catch (TeaException error) {
            log.error(error.message);
        } catch (Exception error) {
            log.error(error.getMessage());
        }
        return null;
    }

    /**
     * 修改解析记录
     */
    public static void updateDomainRecord(com.aliyun.alidns20150109.Client client, UpdateDomainRecordRequest req) throws Exception {
        try {
            UpdateDomainRecordResponse resp = client.updateDomainRecord(req);
            log.info("-------------------修改解析记录--------------------");
            log.info(com.aliyun.teautil.Common.toJSONString(TeaModel.buildMap(resp)));
        } catch (TeaException error) {
            log.error(error.message);
        } catch (Exception error) {
            log.error(error.getMessage());
        }
    }
}
