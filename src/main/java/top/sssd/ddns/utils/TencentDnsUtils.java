package top.sssd.ddns.utils;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.dnspod.v20210323.DnspodClient;
import com.tencentcloudapi.dnspod.v20210323.models.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author sssd
 * @created 2023-05-06-16:13
 */
@Slf4j
public class TencentDnsUtils {

    private TencentDnsUtils() {
    }

    public static final String ENDPOINT = "dnspod.tencentcloudapi.com";

    public static final String RECORDLINE = "默认";

    /**
     * 获取解析记录ID
     *
     * @param domain
     * @param subDomain
     * @param recordType
     * @param secretId
     * @param secretKey
     * @return
     * @throws TencentCloudSDKException
     */
    public static Long getRecordId(String domain, String subDomain, String recordType,
                                   String secretId, String secretKey) throws TencentCloudSDKException {
        RecordListItem[] recordList = getRecordList(domain, subDomain, recordType, secretId, secretKey);
        if (Objects.isNull(recordList)) {
            return null;
        }
        RecordListItem recordListItem = recordList[0];
        return recordListItem.getRecordId();
    }

    /**
     * 根据域名和解析记录获取ip
     *
     * @param domain
     * @param subDomain
     * @param recordType
     * @param secretId
     * @param secretKey
     * @return
     * @throws TencentCloudSDKException
     */
    public static String getIpBySubDomainWithType(String domain, String subDomain, String recordType,
                                                  String secretId, String secretKey) throws TencentCloudSDKException {
        RecordListItem[] recordList = getRecordList(domain, subDomain, recordType, secretId, secretKey);
        if (Objects.isNull(recordList)) {
            return null;
        }
        RecordListItem recordListItem = recordList[0];
        return recordListItem.getValue();
    }

    public static RecordListItem[] getRecordList(String domain, String subDomain, String recordType,
                                                 String secretId, String secretKey) throws TencentCloudSDKException {
        try {
            // 密钥可前往官网控制台 https://console.cloud.tencent.com/cam/capi 进行获取
            Credential cred = new Credential(secretId, secretKey);
            // 实例化一个http选项，可选的，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(ENDPOINT);
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            DnspodClient client = new DnspodClient(cred, "", clientProfile);
            // 实例化一个请求对象,每个接口都会对应一个request对象
            DescribeRecordListRequest req = new DescribeRecordListRequest();
            req.setDomain(domain);
            req.setSubdomain(subDomain);
            req.setRecordType(recordType);
            // 返回的resp是一个DescribeRecordListResponse的实例，与请求对象对应
            DescribeRecordListResponse resp = client.DescribeRecordList(req);
            return resp.getRecordList();
        } catch (TencentCloudSDKException e) {
            if (e.getMessage().contains("记录列表为空")) {
                return new RecordListItem[]{};
            }
            e.printStackTrace();
        }
        return new RecordListItem[]{};
    }

    /**
     * 添加一条解析记录
     *
     * @param domain
     * @param subDomain
     * @param recordType
     * @param ip
     * @return
     * @throws TencentCloudSDKException
     */
    public static CreateRecordResponse createRecord(String domain, String subDomain, String recordType,
                                                    String secretId, String secretKey, String ip) throws TencentCloudSDKException {
        // 实例化一个认证对象，入参需要传入腾讯云账户 SecretId 和 SecretKey，此处还需注意密钥对的保密
        // 代码泄露可能会导致 SecretId 和 SecretKey 泄露，并威胁账号下所有资源的安全性。以下代码示例仅供参考，建议采用更安全的方式来使用密钥，请参见：https://cloud.tencent.com/document/product/1278/85305
        // 密钥可前往官网控制台 https://console.cloud.tencent.com/cam/capi 进行获取
        Credential cred = new Credential(secretId, secretKey);
        // 实例化一个http选项，可选的，没有特殊需求可以跳过
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint(ENDPOINT);
        // 实例化一个client选项，可选的，没有特殊需求可以跳过
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        // 实例化要请求产品的client对象,clientProfile是可选的
        DnspodClient client = new DnspodClient(cred, "", clientProfile);
        // 实例化一个请求对象,每个接口都会对应一个request对象
        CreateRecordRequest req = new CreateRecordRequest();
        req.setDomain(domain);
        req.setSubDomain(subDomain);
        req.setRecordType(recordType);
        req.setRecordLine(RECORDLINE);
        req.setValue(ip);
        // 返回的resp是一个CreateRecordResponse的实例，与请求对象对应
        return client.CreateRecord(req);
    }

    /**
     * 更新记录
     *
     * @param domain
     * @param subDomain
     * @param recordType
     * @param secretId
     * @param secretKey
     * @param ip
     * @param recordId
     * @return
     * @throws TencentCloudSDKException
     */
    public static ModifyRecordResponse updateRecord(String domain, String subDomain, String recordType,
                                                    String secretId, String secretKey, String ip, Long recordId) throws TencentCloudSDKException {
        Credential cred = new Credential(secretId, secretKey);
        // 实例化一个http选项，可选的，没有特殊需求可以跳过
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint(ENDPOINT);
        // 实例化一个client选项，可选的，没有特殊需求可以跳过
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        // 实例化要请求产品的client对象,clientProfile是可选的
        DnspodClient client = new DnspodClient(cred, "", clientProfile);
        // 实例化一个请求对象,每个接口都会对应一个request对象
        ModifyRecordRequest req = new ModifyRecordRequest();
        req.setDomain(domain);
        req.setSubDomain(subDomain);
        req.setRecordType(recordType);
        req.setRecordLine(RECORDLINE);
        req.setValue(ip);
        req.setRecordId(recordId);
        // 返回的resp是一个ModifyRecordResponse的实例，与请求对象对应
        return client.ModifyRecord(req);
    }

    /**
     * 删除解析记录
     *
     * @param domain
     * @param secretId
     * @param secretKey
     * @param recordId
     * @throws TencentCloudSDKException
     */
    public static DeleteRecordResponse deleteRecord(String domain, String secretId, String secretKey, Long recordId) throws TencentCloudSDKException {
        // 密钥可前往官网控制台 https://console.cloud.tencent.com/cam/capi 进行获取
        Credential cred = new Credential(secretId, secretKey);
        // 实例化一个http选项，可选的，没有特殊需求可以跳过
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint(ENDPOINT);
        // 实例化一个client选项，可选的，没有特殊需求可以跳过
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        // 实例化要请求产品的client对象,clientProfile是可选的
        DnspodClient client = new DnspodClient(cred, "", clientProfile);
        // 实例化一个请求对象,每个接口都会对应一个request对象
        DeleteRecordRequest req = new DeleteRecordRequest();
        req.setDomain(domain);
        req.setRecordId(recordId);
        // 返回的resp是一个DeleteRecordResponse的实例，与请求对象对应
        return client.DeleteRecord(req);
    }
}
