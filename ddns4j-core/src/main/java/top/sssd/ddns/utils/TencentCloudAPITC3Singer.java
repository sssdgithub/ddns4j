package top.sssd.ddns.utils;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.TreeMap;

/**
 * @author sssd
 * @careate 2023-12-18-15:03
 */
@Slf4j
public class TencentCloudAPITC3Singer {
    private TencentCloudAPITC3Singer(){}
    private static final  Charset UTF8 = StandardCharsets.UTF_8;
    private static final  String CT_JSON = "application/json; charset=utf-8";
    private static final  String SERVICE = "dnspod";
    public static final  String HOST = "dnspod.tencentcloudapi.com";
    private static final  String VERSION = "2021-03-23";
    private static final  String ALGORITHM = "TC3-HMAC-SHA256";

    private static byte[] hmac256(byte[] key, String msg) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, mac.getAlgorithm());
        mac.init(secretKeySpec);
        return mac.doFinal(msg.getBytes(UTF8));
    }

    private static String sha256Hex(String s) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] d = md.digest(s.getBytes(UTF8));
        return DatatypeConverter.printHexBinary(d).toLowerCase();
    }

    public static TreeMap<String,String> buildSignRequestHeaderWithBody(String secretId, String secretKey, String action, String jsonBody) throws Exception {
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//        log.info("************* TencentCloud: 步骤 1：拼接规范请求串 *************");
        String httpRequestMethod = "POST";
        String canonicalUri = "/";
        String canonicalQueryString = "";
        String canonicalHeaders = "content-type:application/json; charset=utf-8\n"
                + "host:" + HOST + "\n" + "x-tc-action:" + action.toLowerCase() + "\n";
        String signedHeaders = "content-type;host;x-tc-action";
        String hashedRequestPayload = sha256Hex(jsonBody);
        String canonicalRequest = httpRequestMethod + "\n" + canonicalUri + "\n" + canonicalQueryString + "\n"
                + canonicalHeaders + "\n" + signedHeaders + "\n" + hashedRequestPayload;
//        log.info("************* TencentCloud: 步骤 2：拼接待签名字符串 *************");
        String credentialScope = date + "/" + SERVICE + "/" + "tc3_request";
        String hashedCanonicalRequest = sha256Hex(canonicalRequest);
        String stringToSign = ALGORITHM + "\n" + timestamp + "\n" + credentialScope + "\n" + hashedCanonicalRequest;
//        log.info("************* TencentCloud: 步骤 3：计算签名 *************");
        byte[] secretDate = hmac256(("TC3" + secretKey).getBytes(UTF8), date);
        byte[] secretService = hmac256(secretDate, SERVICE);
        byte[] secretSigning = hmac256(secretService, "tc3_request");
        String signature = DatatypeConverter.printHexBinary(hmac256(secretSigning, stringToSign)).toLowerCase();
//        log.info("************* TencentCloud: 步骤 4：拼接 Authorization *************");
        String authorization = ALGORITHM + " " + "Credential=" + secretId + "/" + credentialScope + ", "
                + "SignedHeaders=" + signedHeaders + ", " + "Signature=" + signature;
//        log.info("************* TencentCloud: 步骤 5：拼接 请求头 *************");
        TreeMap<String, String> headers = new TreeMap<>();
        headers.put("Authorization", authorization);
        headers.put("Content-Type", CT_JSON);
        headers.put("Host", HOST);
        headers.put("X-TC-Action", action);
        headers.put("X-TC-Timestamp", timestamp);
        headers.put("X-TC-Version", VERSION);
        return headers;
    }

}


