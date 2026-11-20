package io.foundation.host.endpoint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.foundation.host.audit.AuditService;
import io.foundation.host.support.ApiException;
import io.foundation.host.support.Json;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * One-time credential delivery (ADR-0003). Values stay in memory only, are never persisted, never
 * logged and never returned to the console. A module can receive at most one delivery.
 */
@Service
public class CredentialService {

    public record Pending(String credentialId, String endpointId, String moduleId, ObjectNode payload,
                          Instant expiresAt) {
    }

    private final EndpointRegistry endpoints;
    private final AuditService audit;
    private final Map<String, Pending> byModule = new ConcurrentHashMap<>();
    private final Map<String, Pending> byId = new ConcurrentHashMap<>();
    private final Set<String> deliveredModules = ConcurrentHashMap.newKeySet();

    public CredentialService(EndpointRegistry endpoints, AuditService audit) {
        this.endpoints = endpoints;
        this.audit = audit;
    }

    public Map<String, Object> deliver(String moduleId, String endpointId, JsonNode fields) {
        EndpointRecord endpoint = endpoints.require(endpointId);
        if (!"online".equals(endpoint.status())) {
            throw new ApiException(409, "ENDPOINT_OFFLINE",
                    "endpoint " + endpointId + " is offline, credential cannot be forwarded");
        }
        if (!deliveredModules.add(moduleId)) {
            throw new ApiException(409, "CREDENTIAL_ALREADY_DELIVERED",
                    "module " + moduleId + " already received a one-time credential");
        }
        String credentialId = "cred_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        ObjectNode payload = Json.object();
        payload.put("moduleId", moduleId);
        payload.set("fields", fields != null && fields.isObject() ? fields : Json.object());
        Pending pending = new Pending(credentialId, endpointId, moduleId, payload,
                Instant.now().plusSeconds(60));
        byModule.put(moduleId, pending);
        byId.put(credentialId, pending);
        int fieldCount = fields != null && fields.isObject() ? fields.size() : 0;
        audit.record("admin", "credential.deliver", moduleId,
                "endpoint=" + endpointId + " fields=" + fieldCount + " credential=" + credentialId);
        return Map.of("credentialId", credentialId, "status", "queued", "expiresAt", pending.expiresAt().toString());
    }

    /**
     * Consumes and returns pending credential commands for an endpoint.
     */
    public List<ObjectNode> pollCommands(String endpointId) {
        List<ObjectNode> commands = new ArrayList<>();
        Iterator<Map.Entry<String, Pending>> iterator = byId.entrySet().iterator();
        while (iterator.hasNext()) {
            Pending pending = iterator.next().getValue();
            if (!pending.endpointId().equals(endpointId)) {
                continue;
            }
            if (pending.expiresAt().isBefore(Instant.now())) {
                iterator.remove();
                byModule.remove(pending.moduleId());
                continue;
            }
            ObjectNode command = Json.object();
            command.put("commandId", pending.credentialId());
            command.put("type", "credential.apply");
            command.put("expiresAt", pending.expiresAt().toString());
            command.set("payload", pending.payload());
            commands.add(command);
            iterator.remove();
            byModule.remove(pending.moduleId());
        }
        return commands;
    }

    public void ack(String endpointId, String commandId) {
        Pending pending = byId.remove(commandId);
        if (pending != null && pending.endpointId().equals(endpointId)) {
            byModule.remove(pending.moduleId());
        }
    }
}
