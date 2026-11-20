package io.foundation.host.module;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.foundation.host.audit.AuditService;
import io.foundation.host.endpoint.EndpointRecord;
import io.foundation.host.endpoint.EndpointRegistry;
import io.foundation.host.endpoint.EndpointService;
import io.foundation.host.support.ApiException;
import io.foundation.host.support.Json;
import io.foundation.kernel.manifest.PluginManifest;
import io.foundation.kernel.plugin.PluginCatalog;
import io.foundation.kernel.runtime.ModuleRecord;
import io.foundation.kernel.runtime.ModuleState;
import io.foundation.kernel.runtime.ModuleTarget;
import io.foundation.kernel.runtime.ModuleManager;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Unified module view over host-executed modules and endpoint deployment records.
 */
@Service
public class ModuleFacade {

    public record EndpointModule(String moduleId, String endpointId, String pluginId, String version,
                                 ObjectNode config) {
    }

    private final ModuleManager hostModules;
    private final EndpointService endpoints;
    private final EndpointRegistry endpointRegistry;
    private final PluginCatalog plugins;
    private final AuditService audit;
    private final Map<String, EndpointModule> endpointModules = new ConcurrentHashMap<>();

    public ModuleFacade(ModuleManager hostModules, EndpointService endpoints, EndpointRegistry endpointRegistry,
                        PluginCatalog plugins, AuditService audit) {
        this.hostModules = hostModules;
        this.endpoints = endpoints;
        this.endpointRegistry = endpointRegistry;
        this.plugins = plugins;
        this.audit = audit;
    }

    public Map<String, Object> create(JsonNode body) {
        String pluginId = body.path("pluginId").asText("");
        if (pluginId.isBlank()) {
            throw new ApiException(400, "BAD_REQUEST", "pluginId is required");
        }
        Map<String, Object> config = toMap(body.path("config"));
        JsonNode target = body.path("target");
        String endpointId = target.path("endpointId").asText("");
        boolean host = endpointId.isBlank();

        if (host) {
            ModuleRecord record = hostModules.create(pluginId, ModuleTarget.host(), config);
            try {
                hostModules.start(record.id());
            } catch (RuntimeException failure) {
                audit.record("admin", "module.failed", record.id(), failure.getMessage());
            }
            audit.record("admin", "module.deploy", record.id(), "plugin=" + pluginId + " target=host");
            return hostView(record.id());
        }

        EndpointRecord endpoint = endpointRegistry.require(endpointId);
        PluginManifest manifest = plugins.manifest(pluginId);
        String version = manifest == null ? "0.0.0" : manifest.version();
        String moduleId = "mod_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        ObjectNode module = Json.object();
        module.put("moduleId", moduleId);
        module.put("pluginId", pluginId);
        module.put("version", version);
        module.set("config", Json.mapper().valueToTree(config));
        endpoints.appendModule(endpoint.id(), module);

        EndpointModule record = new EndpointModule(moduleId, endpoint.id(), pluginId, version,
                (ObjectNode) module.path("config"));
        endpointModules.put(moduleId, record);
        audit.record("admin", "module.deploy", moduleId,
                "plugin=" + pluginId + " endpoint=" + endpoint.id());
        return endpointView(record);
    }

    public Map<String, Object> hostView(String moduleId) {
        ModuleRecord record = hostModules.get(moduleId);
        Map<String, Object> view = hostModules.summary(record);
        view.put("target", "host");
        return view;
    }

    public Map<String, Object> endpointView(EndpointModule module) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("moduleId", module.moduleId());
        view.put("pluginId", module.pluginId());
        view.put("version", module.version());
        view.put("target", "endpoint");
        view.put("endpointId", module.endpointId());
        view.put("config", module.config());

        EndpointRecord endpoint = endpointRegistry.require(module.endpointId());
        JsonNode reportedModules = endpoint.reported().path("modules");
        Map<String, Object> reported = null;
        if (reportedModules.isArray()) {
            for (JsonNode item : reportedModules) {
                if (module.moduleId().equals(item.path("moduleId").asText())) {
                    reported = Json.mapper().convertValue(item, Map.class);
                    break;
                }
            }
        }
        view.put("state", reported == null ? "desired" : reported.getOrDefault("state", "reported"));
        view.put("reported", reported);
        view.put("lastError", reported == null ? null : reported.get("lastError"));
        return view;
    }

    public List<Map<String, Object>> list() {
        List<Map<String, Object>> all = new ArrayList<>();
        hostModules.list().forEach(record -> all.add(hostView(record.id())));
        endpointModules.values().forEach(module -> all.add(endpointView(module)));
        return all;
    }

    public Map<String, Object> view(String moduleId) {
        EndpointModule endpointModule = endpointModules.get(moduleId);
        if (endpointModule != null) {
            return endpointView(endpointModule);
        }
        return hostView(moduleId);
    }

    public void start(String moduleId) {
        requireHost(moduleId);
        hostModules.start(moduleId);
        audit.record("admin", "module.start", moduleId, "ok");
    }

    public void stop(String moduleId) {
        requireHost(moduleId);
        hostModules.stop(moduleId);
        audit.record("admin", "module.stop", moduleId, "ok");
    }

    public void reload(String moduleId) {
        requireHost(moduleId);
        hostModules.reload(moduleId);
        audit.record("admin", "module.reload", moduleId, "ok");
    }

    public EndpointModule endpointModule(String moduleId) {
        EndpointModule module = endpointModules.get(moduleId);
        if (module == null) {
            throw new ApiException(404, "MODULE_NOT_FOUND", "module " + moduleId + " does not exist");
        }
        return module;
    }

    public ModuleState hostState(String moduleId) {
        return hostModules.get(moduleId).state();
    }

    public int effectCount(String moduleId) {
        ModuleRecord record = hostModules.get(moduleId);
        if (record.effects() == null) {
            return 0;
        }
        return record.effects().snapshot().children().size();
    }

    private void requireHost(String moduleId) {
        if (endpointModules.containsKey(moduleId)) {
            throw new ApiException(400, "MODULE_NOT_EXECUTABLE",
                    "endpoint modules are deployment records in v1 and are not executed by the host");
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> toMap(JsonNode node) {
        if (node == null || !node.isObject()) {
            return Map.of();
        }
        return Json.mapper().convertValue(node, Map.class);
    }
}
