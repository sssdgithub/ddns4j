package io.foundation.host;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "foundation.plugins-dir=target/test-plugins",
        "foundation.apps-dir=target/test-apps"
})
class EndpointFlowTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Value("${local.server.port}")
    private int port;

    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void enrollTwinReportDriftAndCredentialDelivery() throws Exception {
        String server = "http://127.0.0.1:" + port;

        JsonNode token = data(post(server, "/api/v1/enroll-tokens", null, "{\"ttlSeconds\":3600}", null));
        String enrollToken = token.get("token").asText();

        JsonNode enrolled = data(post(server, "/endpoint/v1/enroll", null,
                "{\"token\":\"" + enrollToken + "\",\"name\":\"router-1\",\"profile\":\"edge\"}", null));
        String endpointId = enrolled.get("endpointId").asText();
        String endpointToken = enrolled.get("endpointToken").asText();
        assertThat(endpointId).startsWith("ep_");
        assertThat(enrolled.get("desiredRevision").asInt()).isEqualTo(1);

        JsonNode again = data(post(server, "/endpoint/v1/enroll", null,
                "{\"token\":\"" + enrollToken + "\"}", null));
        assertThat(again.get("endpointId").asText()).isEqualTo(endpointId);

        post(server, "/endpoint/v1/register", endpointToken,
                "{\"profile\":\"edge\",\"platform\":{\"os\":\"linux\",\"arch\":\"arm64\"},"
                        + "\"version\":\"0.1.0\",\"capabilities\":[{\"id\":\"ip.resolve\"}]}", null);
        JsonNode heartbeat = data(post(server, "/endpoint/v1/heartbeat", endpointToken, "{}", null));
        assertThat(heartbeat.get("desiredRevision").asInt()).isEqualTo(1);
        assertThat(heartbeat.get("serverTime").asText()).isNotBlank();

        JsonNode endpoint = data(get(server, "/api/v1/endpoints/" + endpointId, null));
        assertThat(endpoint.get("profile").asText()).isEqualTo("edge");
        assertThat(endpoint.get("platform").get("arch").asText()).isEqualTo("arm64");
        assertThat(endpoint.get("version").asText()).isEqualTo("0.1.0");

        JsonNode twin = data(get(server, "/endpoint/v1/twin", endpointToken));
        assertThat(twin.get("revision").asInt()).isEqualTo(1);

        post(server, "/endpoint/v1/twin/report", endpointToken,
                "{\"revision\":1,\"config\":{},\"modules\":[],\"state\":\"ok\"}", null);
        endpoint = data(get(server, "/api/v1/endpoints/" + endpointId, null));
        assertThat(endpoint.get("drift").asBoolean()).isFalse();

        JsonNode patched = data(request(server, "PATCH", "/api/v1/endpoints/" + endpointId + "/twin",
                null, "{\"config\":{\"probe\":{\"intervalSec\":30}}}", "1"));
        assertThat(patched.get("revision").asInt()).isEqualTo(2);

        HttpResponse<String> stale = request(server, "PATCH", "/api/v1/endpoints/" + endpointId + "/twin",
                null, "{\"config\":{}}", "1");
        assertThat(stale.statusCode()).isEqualTo(412);

        post(server, "/endpoint/v1/twin/report", endpointToken,
                "{\"revision\":1,\"config\":{},\"modules\":[],\"state\":\"error\","
                        + "\"lastError\":\"apply blocked\"}", null);
        endpoint = data(get(server, "/api/v1/endpoints/" + endpointId, null));
        assertThat(endpoint.get("drift").asBoolean()).isTrue();
        assertThat(endpoint.get("lastError").asText()).contains("apply blocked");

        post(server, "/endpoint/v1/twin/report", endpointToken,
                "{\"revision\":2,\"config\":{\"probe\":{\"intervalSec\":30}},\"modules\":[],\"state\":\"ok\"}", null);
        endpoint = data(get(server, "/api/v1/endpoints/" + endpointId, null));
        assertThat(endpoint.get("drift").asBoolean()).isFalse();

        HttpResponse<String> firstReport = post(server, "/endpoint/v1/report", endpointToken,
                "{\"reportedAt\":\"2026-11-20T00:00:00Z\",\"health\":{\"state\":\"ok\"}}", "idem-1");
        JsonNode firstData = data(firstReport);
        assertThat(firstData.get("duplicate").asBoolean()).isFalse();
        JsonNode secondData = data(post(server, "/endpoint/v1/report", endpointToken,
                "{\"reportedAt\":\"2026-11-20T00:00:00Z\",\"health\":{\"state\":\"ok\"}}", "idem-1"));
        assertThat(secondData.get("duplicate").asBoolean()).isTrue();

        JsonNode module = data(post(server, "/api/v1/modules", null,
                "{\"pluginId\":\"foundation.demo.greeting\",\"target\":{\"endpointId\":\"" + endpointId
                        + "\"},\"config\":{\"greeting\":\"hi\"}}", null));
        String moduleId = module.get("moduleId").asText();
        assertThat(module.get("target").asText()).isEqualTo("endpoint");

        JsonNode delivered = data(post(server, "/api/v1/modules/" + moduleId + ":deliver-credential", null,
                "{\"fields\":{\"accessKeyId\":\"AK-demo\",\"accessKeySecret\":\"secret-value-xyz\"}}", null));
        assertThat(delivered.get("credentialId").asText()).startsWith("cred_");

        HttpResponse<String> duplicate = post(server, "/api/v1/modules/" + moduleId + ":deliver-credential",
                null, "{\"fields\":{\"accessKeyId\":\"AK-demo\"}}", null);
        assertThat(duplicate.statusCode()).isEqualTo(409);

        JsonNode commands = data(get(server, "/endpoint/v1/commands", endpointToken));
        assertThat(commands).hasSize(1);
        assertThat(commands.get(0).get("type").asText()).isEqualTo("credential.apply");
        assertThat(commands.get(0).get("payload").get("fields").get("accessKeySecret").asText())
                .isEqualTo("secret-value-xyz");

        JsonNode audit = data(get(server, "/api/v1/audit", null));
        assertThat(audit.toString()).doesNotContain("secret-value-xyz");

        String expiredToken = data(post(server, "/api/v1/enroll-tokens", null, "{\"ttlSeconds\":1}", null))
                .get("token").asText();
        Thread.sleep(1200);
        HttpResponse<String> expired = post(server, "/endpoint/v1/enroll", null,
                "{\"token\":\"" + expiredToken + "\"}", null);
        assertThat(expired.statusCode()).isEqualTo(401);

        JsonNode offline = data(post(server, "/endpoint/v1/enroll", null,
                "{\"token\":\"" + data(post(server, "/api/v1/enroll-tokens", null, "{}", null)).get("token").asText()
                        + "\"}", null));
        String offlineEndpoint = offline.get("endpointId").asText();
        JsonNode offlineModule = data(post(server, "/api/v1/modules", null,
                "{\"pluginId\":\"foundation.demo.greeting\",\"target\":{\"endpointId\":\"" + offlineEndpoint
                        + "\"}}", null));
        HttpResponse<String> offlineDelivery = post(server,
                "/api/v1/modules/" + offlineModule.get("moduleId").asText() + ":deliver-credential",
                null, "{\"fields\":{\"accessKeyId\":\"AK\"}}", null);
        assertThat(offlineDelivery.statusCode()).isEqualTo(409);
    }

    private JsonNode data(HttpResponse<String> response) throws Exception {
        assertThat(response.statusCode()).isBetween(200, 299);
        return JSON.readTree(response.body()).get("data");
    }

    private HttpResponse<String> post(String server, String path, String bearer, String body,
                                      String idempotencyKey) throws Exception {
        return request(server, "POST", path, bearer, body, null, idempotencyKey);
    }

    private HttpResponse<String> request(String server, String method, String path, String bearer,
                                         String body, String ifMatch) throws Exception {
        return request(server, method, path, bearer, body, ifMatch, null);
    }

    private HttpResponse<String> request(String server, String method, String path, String bearer,
                                         String body, String ifMatch, String idempotencyKey) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(server + path))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .method(method, HttpRequest.BodyPublishers.ofString(body == null ? "" : body));
        if (bearer != null) {
            builder.header("Authorization", "Bearer " + bearer);
        }
        if (ifMatch != null) {
            builder.header("If-Match", ifMatch);
        }
        if (idempotencyKey != null) {
            builder.header("Idempotency-Key", idempotencyKey);
        }
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String server, String path, String bearer) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(server + path))
                .timeout(Duration.ofSeconds(10))
                .GET();
        if (bearer != null) {
            builder.header("Authorization", "Bearer " + bearer);
        }
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}
