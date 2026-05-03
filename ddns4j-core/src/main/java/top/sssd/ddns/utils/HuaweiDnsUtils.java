package top.sssd.ddns.utils;

import com.huaweicloud.sdk.core.auth.BasicCredentials;
import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.dns.v2.DnsClient;
import com.huaweicloud.sdk.dns.v2.model.*;
import com.huaweicloud.sdk.dns.v2.region.DnsRegion;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sssd
 * @create 2023-11-13-21:04
 */
@Slf4j
public class HuaweiDnsUtils {

    /**
     * 查询节点:中国香港
     */
    private static String dnsRegion = "ap-southeast-1";
    /**
     * 查询模式:精确
     */
    private static String searchMode = "equal";

    private HuaweiDnsUtils() {
    }

    public static DnsClient createClient(String secretId, String secretKey) {
        ICredential auth = new BasicCredentials()
                .withAk(secretId)
                .withSk(secretKey);
        return DnsClient.newBuilder()
                .withCredential(auth)
                .withRegion(DnsRegion.valueOf(dnsRegion))
                .build();
    }

    public static String getPublicZoneId(DnsClient dnsClient) {
        ListPublicZonesRequest request = new ListPublicZonesRequest();
        ListPublicZonesResponse response = dnsClient.listPublicZones(request);
        return response.getZones().get(0).getId();
    }

    public static ListRecordSetsResponse listRecordSets(DnsClient dnsClient) {
        ListRecordSetsRequest request = new ListRecordSetsRequest();
        return dnsClient.listRecordSets(request);
    }

    public static ListRecordSetsResponse listRecordSetsByType(DnsClient dnsClient, String recordType) {
        ListRecordSetsRequest request = new ListRecordSetsRequest();
        request.setSearchMode(searchMode);
        request.setType(recordType);
        return dnsClient.listRecordSets(request);
    }

    public static ListRecordSetsResponse listRecordSetsByDomain(DnsClient dnsClient, String domain) {
        ListRecordSetsRequest request = new ListRecordSetsRequest();
        request.setSearchMode(searchMode);
        request.setName(domain);
        return dnsClient.listRecordSets(request);
    }

    public static ListRecordSetsResponse listRecordSetsByDomainWithType(DnsClient dnsClient, String domain, String recordType) {
        ListRecordSetsRequest request = new ListRecordSetsRequest();
        request.setSearchMode(searchMode);
        request.setName(domain);
        request.setType(recordType);
        return dnsClient.listRecordSets(request);
    }

    public static ListRecordSetsResponse listRecordSetsByDomainWithTypeAndIp(DnsClient dnsClient, String domain, String recordType, String ip) {
        ListRecordSetsRequest request = new ListRecordSetsRequest();
        request.setSearchMode(searchMode);
        request.setName(domain);
        request.setType(recordType);
        request.setRecords(ip);
        return dnsClient.listRecordSets(request);
    }

    public static String getRecordId(DnsClient dnsClient, String domain, String recordType, String ip) {
        return listRecordSetsByDomainWithTypeAndIp(dnsClient, domain, recordType, ip).getRecordsets().get(0).getId();
    }

    public static CreateRecordSetResponse add(DnsClient dnsClient, String zoneId, String domain, String recordType, String ip) {
        CreateRecordSetRequest request = new CreateRecordSetRequest();
        request.withZoneId(zoneId);
        CreateRecordSetRequestBody body = new CreateRecordSetRequestBody();
        body.setName(domain);
        body.setType(recordType);
        List<String> ips = new ArrayList<>();
        ips.add(ip);
        body.withRecords(ips);
        request.withBody(body);
        return dnsClient.createRecordSet(request);
    }

    public static UpdateRecordSetResponse update(DnsClient dnsClient, String recordId, String zoneId, String domain, String recordType, String ip) {
        UpdateRecordSetRequest request = new UpdateRecordSetRequest();
        request.withZoneId(zoneId);
        request.withRecordsetId(recordId);
        UpdateRecordSetReq body = new UpdateRecordSetReq();
        List<String> ips = new ArrayList<>();
        ips.add(ip);
        body.withRecords(ips);
        body.withType(recordType);
        body.withName(domain);
        request.withBody(body);
        return dnsClient.updateRecordSet(request);
    }

    public static DeleteRecordSetResponse delete(DnsClient dnsClient, String recordId, String zoneId) {
        DeleteRecordSetRequest request = new DeleteRecordSetRequest();
        request.withZoneId(zoneId);
        request.withRecordsetId(recordId);
        return dnsClient.deleteRecordSet(request);
    }


}
