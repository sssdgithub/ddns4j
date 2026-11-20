package io.foundation.host.endpoint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.foundation.host.audit.AuditService;
import io.foundation.host.support.ApiException;
import io.foundation.host.support.Json;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * One-time enroll tokens. A token may enroll exactly one endpoint and stays valid until it expires;
 * re-enrolling with the same token returns the same endpoint (docs/03 §4).
 */
@Service
public class EnrollTokenService {

    public record EnrollToken(String token, String tenantId, ObjectNode labels, Instant expiresAt,
                              String endpointId) {
    }

    private final Map<String, EnrollToken> tokens = new ConcurrentHashMap<>();
    private final AuditService audit;

    public EnrollTokenService(AuditService audit) {
        this.audit = audit;
    }

    public EnrollToken create(String tenantId, JsonNode labels, long ttlSeconds) {
        long ttl = ttlSeconds <= 0 ? 24 * 3600 : ttlSeconds;
        String value = "et_" + UUID.randomUUID().toString().replace("-", "");
        EnrollToken token = new EnrollToken(value, tenantId == null ? "t_default" : tenantId,
                labels == null || !labels.isObject() ? Json.object() : (ObjectNode) labels,
                Instant.now().plusSeconds(ttl), null);
        tokens.put(value, token);
        audit.record("admin", "enroll-token.create", token.token(), "ttlSeconds=" + ttl);
        return token;
    }

    public Optional<EnrollToken> find(String token) {
        return token == null ? Optional.empty() : Optional.ofNullable(tokens.get(token));
    }

    public EnrollToken require(String token) {
        EnrollToken found = tokens.get(token);
        if (found == null) {
            throw new ApiException(401, "ENROLL_TOKEN_INVALID", "enroll token is unknown");
        }
        if (found.expiresAt().isBefore(Instant.now())) {
            throw new ApiException(401, "ENROLL_TOKEN_EXPIRED", "enroll token expired at " + found.expiresAt());
        }
        return found;
    }

    public synchronized void bind(String token, String endpointId) {
        EnrollToken current = tokens.get(token);
        if (current != null) {
            tokens.put(token, new EnrollToken(current.token(), current.tenantId(), current.labels(),
                    current.expiresAt(), endpointId));
        }
    }
}
