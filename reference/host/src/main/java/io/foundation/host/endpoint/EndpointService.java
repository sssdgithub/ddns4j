package io.foundation.host.endpoint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.foundation.host.audit.AuditService;
import io.foundation.host.config.FoundationProperties;
import io.foundation.host.support.ApiException;
import io.foundation.host.support.Json;
import io.foundation.kernel.twin.MergePatch;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EndpointService {

    public record EnrollResult(String endpointId, String endpointToken, int desiredRevision) {
    }

    public record TwinView(int revision, String etag, JsonNode desired) {
    }

    private final EndpointRegistry registry;
    private final EnrollTokenService enrollTokens;
    private final AuditService audit;
    private final FoundationProperties properties;
    private final Map<String, Long> reportedKeys = new ConcurrentHashMap<>();

    public EndpointService(EndpointRegistry registry, EnrollTokenService enrollTokens, AuditService audit,
                           FoundationProperties properties) {
        this.registry = registry;
        this.enrollTokens = enrollTokens;
        this.audit = audit;
        this.properties = properties;
    }

    public synchronized EnrollResult enroll(String tokenValue, JsonNode body) {
        EnrollTokenService.EnrollToken token = enrollTokens.require(tokenValue);
        if (token.endpointId() != null) {
            EndpointRecord existing = registry.require(token.endpointId());
            return new EnrollResult(existing.id(), existing.endpointToken(),
                    existing.desired().path("revision").asInt());
        }
        ObjectNode labels = token.labels().deepCopy();
        JsonNode bodyLabels = body.path("labels");
        if (bodyLabels.isObject()) {
            bodyLabels.fields().forEachRemaining(field -> labels.set(field.getKey(), field.getValue()));
        }
        EndpointRecord created = registry.create(
                body.path("name").asText("endpoint-" + System.currentTimeMillis()),
                body.path("profile").asText("edge"),
                body.path("platform"),
                body.path("version").asText("0.0.0"),
                body.path("capabilities"),
                labels,
                token.tenantId());
        enrollTokens.bind(tokenValue, created.id());
        audit.record("endpoint", "endpoint.enroll", created.id(), "profile=" + created.profile());
        return new EnrollResult(created.id(), created.endpointToken(), 1);
    }

    public EndpointRecord authenticate(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ApiException(401, "ENDPOINT_UNAUTHORIZED", "endpoint token is required");
        }
        String token = authorization.substring("Bearer ".length()).trim();
        return registry.byToken(token)
                .orElseThrow(() -> new ApiException(401, "ENDPOINT_UNAUTHORIZED", "endpoint token is invalid"));
    }

    public Map<String, Object> register(EndpointRecord endpoint, JsonNode body) {
        if (body.path("profile").isTextual()) {
            endpoint.profile(body.path("profile").asText());
        }
        if (body.path("platform").isObject()) {
            endpoint.platform((ObjectNode) body.path("platform"));
        }
        if (body.path("version").isTextual()) {
            endpoint.version(body.path("version").asText());
        }
        if (body.path("capabilities").isArray()) {
            endpoint.capabilities((ArrayNode) body.path("capabilities"));
        }
        if (body.path("labels").isObject()) {
            endpoint.labels((ObjectNode) body.path("labels"));
        }
        touch(endpoint);
        audit.record("endpoint", "endpoint.register", endpoint.id(),
                "profile=" + endpoint.profile() + " version=" + endpoint.version());
        return view(endpoint);
    }

    public Map<String, Object> heartbeat(EndpointRecord endpoint) {
        touch(endpoint);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("endpointId", endpoint.id());
        payload.put("desiredRevision", endpoint.desired().path("revision").asInt());
        payload.put("serverTime", Instant.now().toString());
        payload.put("intervalSec", 30);
        return payload;
    }

    public TwinView twin(EndpointRecord endpoint) {
        int revision = endpoint.desired().path("revision").asInt();
        return new TwinView(revision, "\"" + revision + "\"", endpoint.desired());
    }

    public Map<String, Object> reportTwin(EndpointRecord endpoint, JsonNode body) {
        ObjectNode reported = Json.object();
        reported.put("revision", body.path("revision").asInt(0));
        reported.set("config", body.path("config").isObject() ? body.path("config") : Json.object());
        reported.set("modules", body.path("modules").isArray() ? body.path("modules") : Json.array());
        reported.put("appliedAt", body.path("appliedAt").asText(Instant.now().toString()));
        reported.put("state", body.path("state").asText("ok"));
        JsonNode lastError = body.path("lastError");
        if (lastError.isTextual() && !lastError.asText().isBlank()) {
            reported.put("lastError", lastError.asText());
            endpoint.lastError(lastError.asText());
        } else {
            reported.putNull("lastError");
            endpoint.lastError(null);
        }
        endpoint.reported(reported);
        touch(endpoint);
        audit.record("endpoint", "twin.report", endpoint.id(), "revision=" + reported.path("revision").asInt());
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("reportedRevision", reported.path("revision").asInt());
        payload.put("drift", drift(endpoint));
        return payload;
    }

    public Map<String, Object> report(EndpointRecord endpoint, String idempotencyKey, JsonNode body) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new ApiException(400, "IDEMPOTENCY_KEY_REQUIRED", "Idempotency-Key header is required");
        }
        purgeExpiredKeys();
        String key = endpoint.id() + "|" + idempotencyKey;
        Long previous = reportedKeys.putIfAbsent(key, System.currentTimeMillis());
        if (previous != null) {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("accepted", true);
            payload.put("duplicate", true);
            return payload;
        }
        touch(endpoint);
        audit.record("endpoint", "endpoint.report", endpoint.id(),
                "health=" + body.path("health").path("state").asText("unknown"));
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("accepted", true);
        payload.put("duplicate", false);
        return payload;
    }

    public synchronized ObjectNode patchTwin(String endpointId, String ifMatch, JsonNode patch) {
        EndpointRecord endpoint = registry.require(endpointId);
        if (ifMatch == null || ifMatch.isBlank()) {
            throw new ApiException(428, "PRECONDITION_REQUIRED", "If-Match header is required");
        }
        int expected;
        try {
            expected = Integer.parseInt(ifMatch.replace("\"", "").replace("W/", "").trim());
        } catch (NumberFormatException failure) {
            throw new ApiException(400, "IF_MATCH_INVALID", "If-Match must be a revision number");
        }
        int current = endpoint.desired().path("revision").asInt();
        if (expected != current) {
            throw new ApiException(412, "REVISION_MISMATCH",
                    "If-Match " + expected + " does not match revision " + current);
        }
        ObjectNode next = (ObjectNode) MergePatch.apply(endpoint.desired(), patch);
        next.put("revision", current + 1);
        endpoint.desired(next);
        audit.record("admin", "twin.patch", endpointId, "revision=" + (current + 1));
        return next;
    }

    public synchronized void appendModule(String endpointId, ObjectNode module) {
        EndpointRecord endpoint = registry.require(endpointId);
        ObjectNode next = endpoint.desired().deepCopy();
        ArrayNode modules = next.withArray("modules");
        modules.add(module);
        next.put("revision", endpoint.desired().path("revision").asInt() + 1);
        endpoint.desired(next);
        audit.record("admin", "module.deploy", module.path("moduleId").asText(),
                "endpoint=" + endpointId + " revision=" + next.path("revision").asInt());
    }

    public boolean drift(EndpointRecord endpoint) {
        return endpoint.desired().path("revision").asInt() != endpoint.reported().path("revision").asInt();
    }

    public String driftLastError(EndpointRecord endpoint) {
        JsonNode reportedError = endpoint.reported().path("lastError");
        if (reportedError.isTextual() && !reportedError.asText().isBlank()) {
            return reportedError.asText();
        }
        if (drift(endpoint)) {
            return "reported revision " + endpoint.reported().path("revision").asInt()
                    + " does not match desired revision " + endpoint.desired().path("revision").asInt();
        }
        return null;
    }

    public Map<String, Object> view(EndpointRecord endpoint) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", endpoint.id());
        view.put("name", endpoint.name());
        view.put("profile", endpoint.profile());
        view.put("platform", endpoint.platform());
        view.put("version", endpoint.version());
        view.put("capabilities", endpoint.capabilities());
        view.put("labels", endpoint.labels());
        view.put("status", endpoint.status());
        view.put("lastSeen", endpoint.lastSeen() == null ? null : endpoint.lastSeen().toString());
        view.put("tenantId", endpoint.tenantId());
        view.put("desired", endpoint.desired());
        view.put("reported", endpoint.reported());
        view.put("desiredRevision", endpoint.desired().path("revision").asInt());
        view.put("reportedRevision", endpoint.reported().path("revision").asInt());
        view.put("drift", drift(endpoint));
        view.put("lastError", driftLastError(endpoint));
        return view;
    }

    public List<Map<String, Object>> list() {
        return registry.all().stream().map(this::view).toList();
    }

    @Scheduled(fixedDelay = 5000)
    public void markStaleOffline() {
        Instant threshold = Instant.now().minusSeconds(properties.getEndpointOfflineSeconds());
        for (EndpointRecord endpoint : registry.all()) {
            Instant seen = endpoint.lastSeen();
            if (seen == null || seen.isBefore(threshold)) {
                if (!"offline".equals(endpoint.status())) {
                    endpoint.status("offline");
                    audit.record("system", "endpoint.offline", endpoint.id(), "lastSeen=" + seen);
                }
            }
        }
    }

    private void touch(EndpointRecord endpoint) {
        endpoint.lastSeen(Instant.now());
        endpoint.status("online");
    }

    private void purgeExpiredKeys() {
        long cutoff = System.currentTimeMillis() - 24L * 3600 * 1000;
        reportedKeys.entrySet().removeIf(entry -> entry.getValue() < cutoff);
    }
}
