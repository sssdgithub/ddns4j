package top.sssd.ddns.service.impl;

import com.aliyun.alidns20150109.Client;
import com.aliyun.alidns20150109.models.DescribeSubDomainRecordsResponse;
import com.aliyun.alidns20150109.models.DescribeSubDomainRecordsResponseBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import top.sssd.ddns.common.BizException;
import top.sssd.ddns.common.enums.RecordTypeEnum;
import top.sssd.ddns.common.utils.DoMainUtil;
import top.sssd.ddns.model.entity.ParsingRecord;
import top.sssd.ddns.service.DynamicDnsService;
import top.sssd.ddns.utils.AliDnsUtils;

/**
 * @author sssd
 * @careate 2023-03-20-13:41
 */
@Service
@Slf4j
public class AliDynamicDnsServiceImpl implements DynamicDnsService {
    @Override
    public boolean exist(String serviceProviderId, String serviceProviderSecret, String subDomain, String recordType) throws Exception {
        Client client = AliDnsUtils.createClient(serviceProviderId, serviceProviderSecret);
        DescribeSubDomainRecordsResponse response = AliDnsUtils.getSubDomainParseList(client, subDomain, recordType);
        if (response.statusCode != HttpStatus.OK.value()) {
            log.error("调用阿里云DNS解析失败,请检查传入的serviceProviderId,serviceProviderSecret,域名是否正确");
            throw new BizException("调用阿里云DNS解析失败,请检查传入的serviceProviderId,serviceProviderSecret,域名是否正确");
        }
        DescribeSubDomainRecordsResponseBody body = response.getBody();
        return body.getTotalCount() > 0 ? true : false;
    }

    @Override
    public void add(ParsingRecord parsingRecord, String ip) {
        //call dns api
        Client client = AliDnsUtils.createClient(parsingRecord.getServiceProviderId(), parsingRecord.getServiceProviderSecret());
        String subDoMain = parsingRecord.getDomain();
        String domain = null;
        String rr = null;
        if (DoMainUtil.firstLevel(subDoMain)) {
            domain = subDoMain;
            rr = "@";
        } else {
            domain = subDoMain.substring(subDoMain.indexOf('.') + 1);
            rr = subDoMain.substring(0, subDoMain.indexOf('.'));
        }
        AliDnsUtils.add(client, domain, rr, RecordTypeEnum.getNameByIndex(parsingRecord.getRecordType()), ip);
    }

    @Override
    public void update(ParsingRecord parsingRecord, String ip,String recordId) throws Exception {
        //call dns api
        Client client = AliDnsUtils.createClient(parsingRecord.getServiceProviderId(), parsingRecord.getServiceProviderSecret());

        String subDoMain = parsingRecord.getDomain();

        String domain = null;
        String rr = null;
        if (DoMainUtil.firstLevel(subDoMain)) {
            domain = subDoMain;
            rr = "@";
        } else {
            domain = subDoMain.substring(subDoMain.indexOf('.') + 1);
            rr = subDoMain.substring(0, subDoMain.indexOf('.'));
        }
        String recordTypeName = RecordTypeEnum.getNameByIndex(parsingRecord.getRecordType());

        AliDnsUtils.update(client, recordId, rr, recordTypeName, ip);
    }

    @Override
    public String getRecordId(ParsingRecord parsingRecord, String ip) throws Exception {
        //call dns api
        Client client = AliDnsUtils.createClient(parsingRecord.getServiceProviderId(), parsingRecord.getServiceProviderSecret());

        String subDoMain = parsingRecord.getDomain();

        String domain = null;
        String rr = null;
        if (DoMainUtil.firstLevel(subDoMain)) {
            domain = subDoMain;
            rr = "@";
        } else {
            domain = subDoMain.substring(subDoMain.indexOf('.') + 1);
            rr = subDoMain.substring(0, subDoMain.indexOf('.'));
        }
        String recordTypeName = RecordTypeEnum.getNameByIndex(parsingRecord.getRecordType());

        String recordId = AliDnsUtils.getDomainRecordId(client, subDoMain, recordTypeName, ip);
        if (StringUtils.isEmpty(recordId)) {
            throw new BizException("没有该域名对应的解析记录");
        }
        return recordId;
    }

    @Override
    public void remove(ParsingRecord parsingRecord, String ip) throws Exception {
        Client client = AliDnsUtils.createClient(parsingRecord.getServiceProviderId(), parsingRecord.getServiceProviderSecret());
        String recordTypeName = RecordTypeEnum.getNameByIndex(parsingRecord.getRecordType());
        String recordId = AliDnsUtils.getDomainRecordId(client, parsingRecord.getDomain(), recordTypeName, ip);
        AliDnsUtils.delete(client, recordId);
    }

    @Override
    public String getIpBySubDomainWithType(ParsingRecord parsingRecord) {
        Client client = AliDnsUtils.createClient(parsingRecord.getServiceProviderId(), parsingRecord.getServiceProviderSecret());
        String ip = AliDnsUtils.getIpBySubDomainWithType(client, parsingRecord.getDomain(), RecordTypeEnum.getNameByIndex(parsingRecord.getRecordType()));
        return ip;
    }
}
