package top.sssd.ddns.utils;

import com.aliyun.alidns20150109.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import top.sssd.ddns.common.BizException;

import java.util.List;

/**
 * @author sssd
 * @update 2022-12-17 23:40
 */
@Slf4j
public class AliDnsUtils {

    private static final String ENDPOINT = "alidns.cn-hangzhou.aliyuncs.com";

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
    public static com.aliyun.alidns20150109.Client createClient(String accessKeyId, String accessKeySecret) throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                // 必填，您的 AccessKey ID
                .setAccessKeyId(accessKeyId)
                // 必填，您的 AccessKey Secret
                .setAccessKeySecret(accessKeySecret);
        config.endpoint = ENDPOINT;
        return new com.aliyun.alidns20150109.Client(config);
    }

    /**
     * 获取子域名的所有解析记录列表
     */
    public static DescribeSubDomainRecordsResponse getSubDomainParseList(com.aliyun.alidns20150109.Client client, String subDomain, String recordType) throws Exception {
        DescribeSubDomainRecordsRequest describeSubDomainRecordsRequest = new DescribeSubDomainRecordsRequest()
                .setSubDomain(subDomain)
                .setType(recordType);
        return client.describeSubDomainRecords(describeSubDomainRecordsRequest);
    }

    /**
     * 获取主域名的所有解析记录列表
     *
     * @param client
     * @param domain
     * @return
     */
    public static DescribeDomainRecordsResponse getParseList(com.aliyun.alidns20150109.Client client, String domain) throws Exception {
        DescribeDomainRecordsRequest describeDomainRecordsRequest = new DescribeDomainRecordsRequest()
                .setDomainName(domain);
        return client.describeDomainRecords(describeDomainRecordsRequest);
    }

    /**
     * 添加一条解析记录
     *
     * @param client
     * @param domain
     * @param Rr
     * @param recordType
     * @param ip
     * @return
     */
    public static AddDomainRecordResponse add(com.aliyun.alidns20150109.Client client, String domain, String Rr, String recordType, String ip) throws Exception {
        AddDomainRecordRequest addDomainRecordRequest = new AddDomainRecordRequest()
                .setDomainName(domain)
                .setRR(Rr)
                .setType(recordType)
                .setValue(ip);
        return client.addDomainRecord(addDomainRecordRequest);
    }

    /**
     * 修改解析记录
     *
     * @param client
     * @param recordId
     * @param Rr
     * @param recordType
     * @param ip
     * @return
     */
    public static UpdateDomainRecordResponse update(com.aliyun.alidns20150109.Client client, String recordId, String Rr, String recordType, String ip) throws Exception {
        UpdateDomainRecordRequest updateDomainRecordRequest = new UpdateDomainRecordRequest()
                .setRecordId(recordId)
                .setRR(Rr)
                .setType(recordType)
                .setValue(ip);
        return client.updateDomainRecord(updateDomainRecordRequest);
    }


    /**
     * 删除解析记录
     * @param client
     * @param recordId
     * @return
     */
    public static DeleteDomainRecordResponse delete(com.aliyun.alidns20150109.Client client,String recordId) throws Exception {
        DeleteDomainRecordRequest deleteDomainRecordRequest = new DeleteDomainRecordRequest()
                .setRecordId(recordId);
        return client.deleteDomainRecord(deleteDomainRecordRequest);
    }


    /**
     * 查询并返回记录ID
     *
     * @param client
     * @param subDomain
     * @param recordType
     * @param ip
     * @return
     */
    public static String getDomainRecordId(com.aliyun.alidns20150109.Client client, String subDomain, String recordType, String ip) throws Exception {
        DescribeSubDomainRecordsResponse response = getSubDomainParseList(client, subDomain, recordType);
        if (response.getStatusCode() != HttpStatus.OK.value()) {
            throw new BizException("查询并返回记录ID时,调用阿里云DNS解析失败,请检查传入的serviceProviderId,serviceProviderSecret,域名是否正确");
        }

        for (DescribeSubDomainRecordsResponseBody.DescribeSubDomainRecordsResponseBodyDomainRecordsRecord domainRecordsRecord :  response.getBody().getDomainRecords().getRecord()) {
            if (ip.equals(domainRecordsRecord.getValue())) {
                return domainRecordsRecord.getRecordId();
            }
        }
        return null;
    }


    /**
     * 根据域名和解析类型查询ip
     * @param client
     * @param subDomain
     * @param recordType
     * @return
     */
    public static String getIpBySubDomainWithType(com.aliyun.alidns20150109.Client client,String subDomain, String recordType) throws Exception {
        DescribeSubDomainRecordsRequest describeSubDomainRecordsRequest = new DescribeSubDomainRecordsRequest()
                .setSubDomain(subDomain)
                .setType(recordType);
        DescribeSubDomainRecordsResponse response = client.describeSubDomainRecords(describeSubDomainRecordsRequest);
        List<DescribeSubDomainRecordsResponseBody.DescribeSubDomainRecordsResponseBodyDomainRecordsRecord> records = response.getBody().getDomainRecords().getRecord();
        return records.get(0).getValue();
    }
}
