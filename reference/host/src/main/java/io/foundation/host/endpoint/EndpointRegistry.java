package io.foundation.host.endpoint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.foundation.host.support.Json;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EndpointRegistry {

    private final Map<String, EndpointRecord> endpoints = new ConcurrentHashMap<>();
    private final Map<String, EndpointRecord> byToken = new ConcurrentHashMap<>();

    public EndpointRecord create(String name, String profile, JsonNode platform, String version,
                                 JsonNode capabilities, JsonNode labels, String tenantId) {
        String id = "ep_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String token = "ept_" + UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        EndpointRecord record = new EndpointRecord(id, tenantId, token);
        record.name(name);
        record.profile(profile == null || profile.isBlank() ? "edge" : profile);
        record.platform(platform == null || !platform.isObject() ? Json.object() : (ObjectNode) platform);
        record.version(version == null || version.isBlank() ? "0.0.0" : version);
        record.capabilities(capabilities == null || !capabilities.isArray() ? Json.array() : (ArrayNode) capabilities);
        record.labels(labels == null || !labels.isObject() ? Json.object() : (ObjectNode) labels);
        record.desired(initialDesired());
        record.reported(initialReported());
        endpoints.put(id, record);
        byToken.put(token, record);
        return record;
    }

    public Optional<EndpointRecord> byToken(String token) {
        return token == null ? Optional.empty() : Optional.ofNullable(byToken.get(token));
    }

    public List<EndpointRecord> all() {
        return endpoints.values().stream()
                .sorted(Comparator.comparing(EndpointRecord::createdAt))
                .toList();
    }

    public EndpointRecord require(String endpointId) {
        EndpointRecord record = endpoints.get(endpointId);
        if (record == null) {
            throw new io.foundation.host.support.ApiException(404, "ENDPOINT_NOT_FOUND",
                    "endpoint " + endpointId + " does not exist");
        }
        return record;
    }

    public Optional<EndpointRecord> find(String endpointId) {
        return Optional.ofNullable(endpoints.get(endpointId));
    }

    private ObjectNode initialDesired() {
        ObjectNode node = Json.object();
        node.put("revision", 1);
        node.set("config", Json.object());
        node.set("modules", Json.array());
        return node;
    }

    private ObjectNode initialReported() {
        ObjectNode node = Json.object();
        node.put("revision", 0);
        node.set("config", Json.object());
        node.set("modules", Json.array());
        node.putNull("appliedAt");
        node.put("state", "unknown");
        node.putNull("lastError");
        return node;
    }
}
