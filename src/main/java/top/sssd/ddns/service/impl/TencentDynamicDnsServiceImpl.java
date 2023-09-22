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
 * @created 2023-05-06-17:11
 */
@Service
public class TencentDynamicDnsServiceImpl implements DynamicDnsService {


    @Override
    public boolean exist(String serviceProviderId, String serviceProviderSecret, String domain, String recordType) throws TencentCloudSDKException {
        String resultDomain = "";
        String subDoMain = "";
        if (DoMainUtil.firstLevel(domain)) {
            subDoMain = "@";
        } else {
            resultDomain = domain.substring(domain.indexOf('.') + 1);
            subDoMain = domain.substring(0, domain.indexOf('.'));
        }
        RecordListItem[] recordArray = TencentDnsUtils.getRecordList(resultDomain, subDoMain, recordType, serviceProviderId, serviceProviderSecret);
        return Objects.nonNull(recordArray) && recordArray.length > 0;
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
    public void update(ParsingRecord parsingRecord, String ip, String recordId) {
        String domain = parsingRecord.getDomain();
        String resultDomain = "";
        String subDoMain = "";
        if (DoMainUtil.firstLevel(domain)) {
            subDoMain = "@";
        } else {
            resultDomain = domain.substring(domain.indexOf('.') + 1);
            subDoMain = domain.substring(0, domain.indexOf('.'));
        }
        try {
            TencentDnsUtils.updateRecord(resultDomain, subDoMain, RecordTypeEnum.getNameByIndex(parsingRecord.getRecordType()), parsingRecord.getServiceProviderId(), parsingRecord.getServiceProviderSecret(), ip, Long.parseLong(recordId));
        } catch (TencentCloudSDKException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getRecordId(ParsingRecord parsingRecord, String ip) throws TencentCloudSDKException {
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
    public void remove(ParsingRecord parsingRecord, String ip) throws TencentCloudSDKException {
        String domain = parsingRecord.getDomain();
        String resultDomain = domain.substring(domain.indexOf('.') + 1);
        String recordId = getRecordId(parsingRecord, ip);
        try {
            TencentDnsUtils.deleteRecord(resultDomain, parsingRecord.getServiceProviderId(), parsingRecord.getServiceProviderSecret(), Long.parseLong(recordId));
        } catch (TencentCloudSDKException e) {
            e.printStackTrace();
        }
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
        return TencentDnsUtils.getIpBySubDomainWithType(resultDomain, subDoMain, RecordTypeEnum.getNameByIndex(parsingRecord.getRecordType()), parsingRecord.getServiceProviderId(), parsingRecord.getServiceProviderSecret());
    }
}
