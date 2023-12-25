package top.sssd.ddns.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import static top.sssd.ddns.utils.TencentCloudAPITC3Singer.HOST;


/**
 * @author sssd
 * @created 2023-05-06-16:13
 */
@Slf4j
@Component
public class TencentDnsUtils {

    private TencentDnsUtils() {
    }

    private static final String CREATE_RECORD_ACTION = "CreateRecord";
    private static final String MODIFY_RECORD_ACTION = "ModifyRecord";
    private static final String DELETE_RECORD_ACTION = "DeleteRecord";
    private static final String DESCRIBE_RECORD_LIST_ACTION = "DescribeRecordList";

    private static RestTemplate staticRestTemplate;


    private static ObjectMapper staticObjectMapper;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @PostConstruct
    public void init(){
        staticRestTemplate = restTemplate;
        staticObjectMapper = objectMapper;
    }

    /**
     * 创建记录实体
     * @ https://cloud.tencent.com/document/product/1427/56180
     */
    @Data
    @Accessors(chain = true)
    public static class CreateRecordRequest {
        @JsonProperty("Domain")
        private String domain;
        @JsonProperty("SubDomain")
        private String subDomain;
        @JsonProperty("RecordType")
        private String recordType;
        @JsonProperty("Value")
        private String value;
        @JsonProperty("RecordLine")
        private String recordLine = "默认";
    }

    @Data
    public static class CreateRecordResponse {
        @JsonProperty("Response")
        private CreateResponse response;
    }

    @Data
    private static class CreateResponse{
        @JsonProperty("RecordId")
        private String recordId;
        @JsonProperty("RequestId")
        private String requestId;
    }

    /**
     * 删除记录实体
     * @ https://cloud.tencent.com/document/product/1427/56176
     */
    @Data
    @Accessors(chain = true)
    public static class DeleteRecordRequest {
        @JsonProperty("Domain")
        private String domain;
        @JsonProperty("RecordId")
        private Integer recordId;
    }

    @Data
    public static class DeleteRecordResponse {
        @JsonProperty("Response")
        private DeleteResponse response;
    }

    @Data
    public static class DeleteResponse{
        @JsonProperty("RequestId")
        private String requestId;
    }

    /**
     * 获取列表实体
     * @ https://cloud.tencent.com/document/product/1427/56166
     */
    @Data
    @Accessors(chain = true)
    public static class ListRecordRequest {
        @JsonProperty("Domain")
        private String domain;
        @JsonProperty("Subdomain")
        private String subDomain;
        @JsonProperty("RecordType")
        private String recordType;
        @JsonProperty("RecordLine")
        private String recordLine = "默认";
    }

    @Data
    public static class ListRecordResponse {
        @JsonProperty("Response")
        private ListResponse response;
    }

    @Data
    public static class ListResponse {
        @JsonProperty("RequestId")
        private String requestId;
        @JsonProperty("RecordList")
        private List<RecordListItem> recordList = new ArrayList<>();
    }

    @Data
    public static class RecordListItem {
        @JsonProperty("RecordId")
        private String recordId;
        @JsonProperty("Value")
        private String value;
    }

    /**
     * 更新记录实体对象
     * @ https://cloud.tencent.com/document/product/1427/56157
     */
    @Data
    @Accessors(chain = true)
    public static class UpdateRecordRequest {
        @JsonProperty("Domain")
        private String domain;
        @JsonProperty("SubDomain")
        private String subDomain;
        @JsonProperty("RecordType")
        private String recordType;
        @JsonProperty("RecordId")
        private Integer recordId;
        @JsonProperty("Value")
        private String value;
        @JsonProperty("RecordLine")
        private String recordLine = "默认";
    }

    @Data
    public static class UpdateRecordResponse {
        @JsonProperty("Response")
        private CreateResponse response;
    }

    @Data
    private static class UpdateResponse{
        @JsonProperty("RecordId")
        private Integer recordId;
        @JsonProperty("RequestId")
        private String requestId;
    }

    /**
     * 获取解析记录ID
     *
     * @param domain
     * @param subDomain
     * @param recordType
     * @param secretId
     * @param secretKey
     * @return
     */
    public static String getRecordId(String domain, String subDomain, String recordType,
                                   String secretId, String secretKey) throws Exception {
        ListRecordResponse recordList = getRecordList(domain, subDomain, recordType, secretId, secretKey);
        return recordList.getResponse().getRecordList().get(0).getRecordId();
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
     */
    public static String getIpBySubDomainWithType(String domain, String subDomain, String recordType,
                                                  String secretId, String secretKey) throws Exception {
        ListRecordResponse recordList = getRecordList(domain, subDomain, recordType, secretId, secretKey);
        return recordList.getResponse().getRecordList().get(0).getValue();
    }

    public static ListRecordResponse getRecordList(String domain, String subDomain, String recordType,
                                                 String secretId, String secretKey) throws Exception {
        ListRecordRequest listRecordRequest = new ListRecordRequest().setDomain(domain).setSubDomain(subDomain).setRecordType(recordType);
        String jsonBody = staticObjectMapper.writeValueAsString(listRecordRequest);

        TreeMap<String, String> headerMap = TencentCloudAPITC3Singer.buildSignRequestHeaderWithBody(secretId, secretKey, DESCRIBE_RECORD_LIST_ACTION, jsonBody);
        HttpHeaders headers = new HttpHeaders();
        headers.setAll(headerMap);

        // 构建URI参数
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("https://" + HOST);
        // 构建RequestEntity
        RequestEntity<String> requestEntity = new RequestEntity<String>(jsonBody, headers, HttpMethod.POST, builder.build().toUri());
        //发送请求
        ResponseEntity<ListRecordResponse> response = staticRestTemplate.exchange(requestEntity, ListRecordResponse.class);
        return response.getBody();
    }

    /**
     * 添加一条解析记录
     *
     * @param domain
     * @param subDomain
     * @param recordType
     * @param ip
     * @return
     * @throws Exception
     */
    public static CreateRecordResponse createRecord(String domain, String subDomain, String recordType,
                                                    String secretId, String secretKey, String ip) throws Exception {
        CreateRecordRequest createRecordRequest = new CreateRecordRequest().setDomain(domain).setSubDomain(subDomain).setRecordType(recordType).setValue(ip);
        String jsonBody = staticObjectMapper.writeValueAsString(createRecordRequest);

        TreeMap<String, String> headerMap = TencentCloudAPITC3Singer.buildSignRequestHeaderWithBody(secretId, secretKey, CREATE_RECORD_ACTION, jsonBody);
        HttpHeaders headers = new HttpHeaders();
        headers.setAll(headerMap);
        // 构建URI参数
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("https://" + HOST);
        // 构建RequestEntity
        RequestEntity<String> requestEntity = new RequestEntity<String>(jsonBody, headers, HttpMethod.POST, builder.build().toUri());
        //发送请求
        ResponseEntity<CreateRecordResponse> response = staticRestTemplate.exchange(requestEntity, CreateRecordResponse.class);
        return response.getBody();
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
     */
    public static UpdateRecordResponse updateRecord(String domain, String subDomain, String recordType,
                                                    String secretId, String secretKey, String ip, Integer recordId) throws Exception {
        UpdateRecordRequest updateRecordRequest = new UpdateRecordRequest().setDomain(domain).setValue(ip).setSubDomain(subDomain).setRecordType(recordType).setRecordId(recordId);
        String jsonBody = staticObjectMapper.writeValueAsString(updateRecordRequest);

        TreeMap<String, String> headerMap = TencentCloudAPITC3Singer.buildSignRequestHeaderWithBody(secretId, secretKey, MODIFY_RECORD_ACTION, jsonBody);
        HttpHeaders headers = new HttpHeaders();
        headers.setAll(headerMap);
        // 构建URI参数
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("https://" + HOST);
        // 构建RequestEntity
        RequestEntity<String> requestEntity = new RequestEntity<String>(jsonBody, headers, HttpMethod.POST, builder.build().toUri());
        //发送请求
        ResponseEntity<UpdateRecordResponse> response = staticRestTemplate.exchange(requestEntity, UpdateRecordResponse.class);
        return response.getBody();
    }

    /**
     * 删除解析记录
     *
     * @param domain
     * @param secretId
     * @param secretKey
     * @param recordId
     */
    public static DeleteRecordResponse deleteRecord(String domain, String secretId, String secretKey, Integer recordId) throws Exception {
        DeleteRecordRequest deleteRecordRequest = new DeleteRecordRequest().setDomain(domain).setRecordId(recordId);
        String jsonBody = staticObjectMapper.writeValueAsString(deleteRecordRequest);

        TreeMap<String, String> headerMap = TencentCloudAPITC3Singer.buildSignRequestHeaderWithBody(secretId, secretKey, DELETE_RECORD_ACTION, jsonBody);
        HttpHeaders headers = new HttpHeaders();
        headers.setAll(headerMap);
        // 构建URI参数
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("https://" + HOST);
        // 构建RequestEntity
        RequestEntity<String> requestEntity = new RequestEntity<String>(jsonBody, headers, HttpMethod.POST, builder.build().toUri());
        //发送请求
        ResponseEntity<DeleteRecordResponse> response = staticRestTemplate.exchange(requestEntity, DeleteRecordResponse.class);
        return response.getBody();
    }
}
