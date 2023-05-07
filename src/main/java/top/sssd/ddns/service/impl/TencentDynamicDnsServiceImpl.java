package top.sssd.ddns.service.impl;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.dnspod.v20210323.models.RecordListItem;
import org.springframework.stereotype.Service;
import top.sssd.ddns.common.enums.RecordTypeEnum;
import top.sssd.ddns.common.utils.DoMainUtil;
import top.sssd.ddns.model.entity.ParsingRecord;
import top.sssd.ddns.service.DynamicDnsService;
import top.sssd.ddns.utils.TencentDnsUtils;

import java.util.Objects;

/**
 * @author sssd
 * @careate 2023-05-06-17:11
 */
@Service
public class TencentDynamicDnsServiceImpl implements DynamicDnsService {


    @Override
    public boolean exist(String serviceProviderId, String serviceProviderSecret, String domain, String recordType) throws Exception {
        String resultDomain = "";
        String subDoMain = "";
        if (DoMainUtil.firstLevel(domain)) {
            subDoMain = "@";
        } else {
            resultDomain = domain.substring(domain.indexOf('.') + 1);
            subDoMain = domain.substring(0, domain.indexOf('.'));
        }
        RecordListItem[] recordArray = TencentDnsUtils.getRecordList(resultDomain, subDoMain, recordType, serviceProviderId, serviceProviderSecret);
        return Objects.nonNull(recordArray) ? true : false;
    }

    @Override
    public void add(ParsingRecord parsingRecord, String ip) throws TencentCloudSDKException {
        String domain = parsingRecord.getDomain();
        String resultDomain = "";
        String subDoMain = "";
        if (DoMainUtil.firstLevel(domain)) {
            subDoMain = "@";
        } else {
            resultDomain = domain.substring(domain.indexOf('.') + 1);
            subDoMain = domain.substring(0, domain.indexOf('.'));
        }
        TencentDnsUtils.createRecord(resultDomain, subDoMain, RecordTypeEnum.getNameByIndex(parsingRecord.getRecordType()), parsingRecord.getServiceProviderId(), parsingRecord.getServiceProviderSecret(), ip);
    }

    @Override
    public void update(ParsingRecord parsingRecord, String ip, String recordId) throws Exception {
        String domain = parsingRecord.getDomain();
        String resultDomain = "";
        String subDoMain = "";
        if (DoMainUtil.firstLevel(domain)) {
            subDoMain = "@";
        } else {
            resultDomain = domain.substring(domain.indexOf('.') + 1);
            subDoMain = domain.substring(0, domain.indexOf('.'));
        }
        TencentDnsUtils.updateRecord(resultDomain, subDoMain, RecordTypeEnum.getNameByIndex(parsingRecord.getRecordType()), parsingRecord.getServiceProviderId(), parsingRecord.getServiceProviderSecret(), ip, Long.parseLong(recordId));
    }

    @Override
    public String getRecordId(ParsingRecord parsingRecord, String ip) throws Exception {
        String domain = parsingRecord.getDomain();
        String resultDomain = "";
        String subDoMain = "";
        if (DoMainUtil.firstLevel(domain)) {
            subDoMain = "@";
        } else {
            resultDomain = domain.substring(domain.indexOf('.') + 1);
            subDoMain = domain.substring(0, domain.indexOf('.'));
        }
        Long recordId = TencentDnsUtils.getRecordId(resultDomain, subDoMain, RecordTypeEnum.getNameByIndex(parsingRecord.getRecordType()), parsingRecord.getServiceProviderId(), parsingRecord.getServiceProviderSecret());
        return recordId.toString();
    }

    @Override
    public void remove(ParsingRecord parsingRecord, String ip) throws Exception {
        String domain = parsingRecord.getDomain();
        String resultDomain = "";
        String subDoMain = "";
        if (DoMainUtil.firstLevel(domain)) {
            subDoMain = "@";
        } else {
            resultDomain = domain.substring(domain.indexOf('.') + 1);
            subDoMain = domain.substring(0, domain.indexOf('.'));
        }
        String recordId = getRecordId(parsingRecord, ip);
        // TODO: 2023/5/6 待验证
        TencentDnsUtils.deleteRecord(resultDomain, parsingRecord.getServiceProviderId(), parsingRecord.getServiceProviderSecret(), Long.parseLong(recordId));
    }

    @Override
    public String getIpBySubDomainWithType(ParsingRecord parsingRecord) throws TencentCloudSDKException {
        String domain = parsingRecord.getDomain();
        String resultDomain = "";
        String subDoMain = "";
        if (DoMainUtil.firstLevel(domain)) {
            subDoMain = "@";
        } else {
            resultDomain = domain.substring(domain.indexOf('.') + 1);
            subDoMain = domain.substring(0, domain.indexOf('.'));
        }
        String dnsIp = TencentDnsUtils.getIpBySubDomainWithType(resultDomain, subDoMain, RecordTypeEnum.getNameByIndex(parsingRecord.getRecordType()), parsingRecord.getServiceProviderId(), parsingRecord.getServiceProviderSecret());
        return dnsIp;
    }
}
