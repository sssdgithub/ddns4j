package top.sssd.ddns.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

/**
 * @author sssd
 * @created 2023-08-08-10:57
 */

public class CloudflareUtils {
    private CloudflareUtils() {
    }

    @Data
    public static class CloudflareQueryResponse {
        private List<Map> errors;
        private List<Map> messages;
        private Boolean success;
        private List<SimpleContent> result;
    }

    @Data
    public static class CloudflareResponse {
        private List<Map> errors;
        private List<Map> messages;
        private Boolean success;
        private SimpleContent result;
    }

    @Data
    @Accessors(chain = true)
    public static class SimpleContent {
        private String name;
        private Boolean proxied = true;
        private String content;
        private String type;
        private String id;
        private Boolean locked;
        private Map meta;
        private List<String> tags;
    }

    private static RestTemplate restTemplate = new RestTemplate();

    private static final String BASE_URL = "https://api.cloudflare.com/client/v4/zones/";

    private static final String AUTH_HEADER = "Authorization";

    private static final String BEARER = "bearer";
    
    private static final String ROUTE_PATH = "/dns_records";


    /**
     * 根据子域名和解析类型查询域名列表
     *
     * @param zoneId          区域ID
     * @param accessKeySecret API_TOKEN
     * @param subDomain       子域名
     * @param recordType      解析类型
     * @return
     */
    public static CloudflareQueryResponse getSubDomainParseList(String zoneId, String accessKeySecret, String subDomain, String recordType) {
        // 设置请求头，包括Authorization和Content-Type
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTH_HEADER, BEARER + " " + accessKeySecret);
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 构建URI参数
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(BASE_URL)
                .path(zoneId + ROUTE_PATH)
                .queryParam("type", recordType)
                .queryParam("name", subDomain);
        // 构建RequestEntity
        RequestEntity<?> requestEntity = new RequestEntity<>(headers, HttpMethod.GET, builder.build().toUri());
        //发送请求
        ResponseEntity<CloudflareQueryResponse> response = restTemplate.exchange(requestEntity, CloudflareQueryResponse.class);
        return response.getBody();
    }

    public static CloudflareQueryResponse getSubDomainParseList(String zoneId, String accessKeySecret, String subDomain, String recordType, String ip) {
        // 设置请求头，包括Authorization和Content-Type
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTH_HEADER, BEARER + " " + accessKeySecret);
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 构建URI参数
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(BASE_URL)
                .path(zoneId + ROUTE_PATH)
                .queryParam("type", recordType)
                .queryParam("name", subDomain)
                .queryParam("content", ip);
        // 构建RequestEntity
        RequestEntity<?> requestEntity = new RequestEntity<>(headers, HttpMethod.GET, builder.build().toUri());
        //发送请求
        ResponseEntity<CloudflareQueryResponse> response = restTemplate.exchange(requestEntity, CloudflareQueryResponse.class);
        return response.getBody();
    }


    /**
     * @param zoneId          区域ID
     * @param accessKeySecret API_TOKEN
     * @param domain          子域名
     * @param recordType      解析类型
     * @param ip              解析IP
     * @return
     * @throws JsonProcessingException
     */
    public static CloudflareResponse add(String zoneId, String accessKeySecret, String domain, String recordType, String ip) throws JsonProcessingException {
        // 设置请求头，包括Authorization和Content-Type
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTH_HEADER, BEARER + " " + accessKeySecret);
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 构建URI参数
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(BASE_URL).path(zoneId + ROUTE_PATH);

        SimpleContent simpleContent = new SimpleContent().setType(recordType).setName(domain).setContent(ip);
        // 构建RequestEntity
        RequestEntity<SimpleContent> requestEntity = new RequestEntity<>(simpleContent, headers, HttpMethod.POST, builder.build().toUri());
        //发送请求
        ResponseEntity<CloudflareResponse> response = restTemplate.exchange(requestEntity, CloudflareResponse.class);
        return response.getBody();
    }

    /**
     * 获取记录ID
     *
     * @param zoneId
     * @param accessKeySecret
     * @param subDomain
     * @param recordType
     * @return
     */
    public static String getId(String zoneId, String accessKeySecret, String subDomain, String recordType, String ip) {
        CloudflareQueryResponse response = getSubDomainParseList(zoneId, accessKeySecret, subDomain, recordType, ip);
        return response.getResult().get(0).getId();
    }


    /**
     * @param zoneId
     * @param accessKeySecret
     * @param subDomain
     * @param recordType
     * @return
     */
    public static String getIpBySubDomainWithType(String zoneId, String accessKeySecret, String subDomain, String recordType) {
        CloudflareQueryResponse cloudflareResponse = getSubDomainParseList(zoneId, accessKeySecret, subDomain, recordType);
        return cloudflareResponse.getResult().get(0).getContent();
    }

    /**
     * 删除记录
     *
     * @param zoneId
     * @param accessKeySecret
     * @param domain
     * @param recordType
     * @return
     */
    public static CloudflareResponse delete(String zoneId, String accessKeySecret, String domain, String recordType,String ip) {
        // 设置请求头，包括Authorization和Content-Type
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTH_HEADER, BEARER + " " + accessKeySecret);
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 构建URI参数
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(BASE_URL)
                .path(zoneId + ROUTE_PATH)
                .path("/" + getId(zoneId, accessKeySecret, domain, recordType,ip));

        // 构建RequestEntity
        RequestEntity<?> requestEntity = new RequestEntity<>(headers, HttpMethod.DELETE, builder.build().toUri());
        //发送请求
        ResponseEntity<CloudflareResponse> response = restTemplate.exchange(requestEntity, CloudflareResponse.class);
        return response.getBody();
    }


    /**
     * 更新记录
     *
     * @param zoneId
     * @param accessKeySecret
     * @param domain
     * @param recordType
     * @param ip
     * @return
     */
    public static CloudflareResponse update(String zoneId, String accessKeySecret, String domain, String recordType, String ip,String recordId) {
        // 设置请求头，包括Authorization和Content-Type
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTH_HEADER, BEARER + " " + accessKeySecret);
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 构建URI参数
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(BASE_URL)
                .path(zoneId + ROUTE_PATH)
                .path("/" + recordId);

        SimpleContent simpleContent = new SimpleContent().setType(recordType).setName(domain).setContent(ip);
        // 构建RequestEntity
        RequestEntity<SimpleContent> requestEntity = new RequestEntity<>(simpleContent, headers, HttpMethod.PUT, builder.build().toUri());
        //发送请求
        ResponseEntity<CloudflareResponse> response = restTemplate.exchange(requestEntity, CloudflareResponse.class);
        return response.getBody();
    }
}
