package io.foundation.host.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.foundation.host.app.AppCatalogService;
import io.foundation.host.audit.AuditService;
import io.foundation.host.endpoint.CredentialService;
import io.foundation.host.endpoint.EndpointRegistry;
import io.foundation.host.endpoint.EndpointService;
import io.foundation.host.endpoint.EnrollTokenService;
import io.foundation.host.module.ModuleFacade;
import io.foundation.host.support.Api;
import io.foundation.host.support.Json;
import io.foundation.kernel.http.RouteRegistry;
import io.foundation.kernel.manifest.PluginManifest;
import io.foundation.kernel.plugin.PluginCatalog;
import io.foundation.kernel.service.HostServiceRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Control-plane API. v1 binds to localhost with a single administrator; RBAC is out of scope.
 */
@RestController
@RequestMapping("/api/v1")
public class AdminApiController {

    private final PluginCatalog plugins;
    private final HostServiceRegistry services;
    private final RouteRegistry routes;
    private final ModuleFacade modules;
    private final EndpointService endpoints;
    private final EndpointRegistry endpointRegistry;
    private final EnrollTokenService enrollTokens;
    private final CredentialService credentials;
    private final AuditService audit;
    private final AppCatalogService apps;

    public AdminApiController(PluginCatalog plugins, HostServiceRegistry services, RouteRegistry routes,
                              ModuleFacade modules, EndpointService endpoints, EndpointRegistry endpointRegistry,
                              EnrollTokenService enrollTokens, CredentialService credentials, AuditService audit,
                              AppCatalogService apps) {
        this.plugins = plugins;
        this.services = services;
        this.routes = routes;
        this.modules = modules;
        this.endpoints = endpoints;
        this.endpointRegistry = endpointRegistry;
        this.enrollTokens = enrollTokens;
        this.credentials = credentials;
        this.audit = audit;
        this.apps = apps;
    }

    @GetMapping("/plugins")
    public ResponseEntity<String> plugins() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (var plugin : plugins.plugins()) {
            PluginManifest manifest = plugin.manifest();
            Map<String, Object> view = new LinkedHashMap<>();
            view.put("id", manifest.id());
            view.put("name", manifest.name());
            view.put("version", manifest.version());
            view.put("kind", manifest.kind());
            view.put("provides", manifest.provides());
            view.put("requires", manifest.requires());
            view.put("runtime", manifest.runtime() == null ? null : manifest.runtime().type());
            view.put("source", plugin.source());
            list.add(view);
        }
        return Api.ok(list);
    }

    @GetMapping("/services")
    public ResponseEntity<String> services() {
        List<Map<String, Object>> list = new ArrayList<>();
        services.owners().forEach((serviceId, owner) -> {
            Map<String, Object> view = new LinkedHashMap<>();
            view.put("serviceId", serviceId);
            view.put("owner", owner);
            view.put("available", services.available(serviceId));
            list.add(view);
        });
        return Api.ok(list);
    }

    @GetMapping("/routes")
    public ResponseEntity<String> routes() {
        List<Map<String, Object>> list = routes.routes().stream()
                .map(route -> {
                    Map<String, Object> view = new LinkedHashMap<>();
                    view.put("method", route.method());
                    view.put("path", route.path());
                    view.put("moduleId", route.moduleId());
                    return view;
                })
                .toList();
        return Api.ok(list);
    }

    @GetMapping("/apps")
    public ResponseEntity<String> apps() {
        return Api.ok(apps.list());
    }

    @PostMapping("/enroll-tokens")
    public ResponseEntity<String> createEnrollToken(@RequestBody(required = false) String body) {
        JsonNode request = Json.read(body);
        EnrollTokenService.EnrollToken token = enrollTokens.create(
                request.path("tenantId").asText("t_default"),
                request.path("labels"),
                request.path("ttlSeconds").asLong(0));
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("token", token.token());
        payload.put("endpointId", token.endpointId());
        payload.put("expiresAt", token.expiresAt().toString());
        return Api.created(payload);
    }

    @GetMapping("/endpoints")
    public ResponseEntity<String> endpoints() {
        return Api.ok(endpoints.list());
    }

    @GetMapping("/endpoints/{id}")
    public ResponseEntity<String> endpoint(@PathVariable String id) {
        return Api.ok(endpoints.view(endpointRegistry.require(id)));
    }

    @PatchMapping("/endpoints/{id}/twin")
    public ResponseEntity<String> patchTwin(@PathVariable String id,
                                            @RequestHeader(value = "If-Match", required = false) String ifMatch,
                                            @RequestBody String body) {
        ObjectNode desired = endpoints.patchTwin(id, ifMatch, Json.read(body));
        return Api.ok(desired);
    }

    @GetMapping("/modules")
    public ResponseEntity<String> modules() {
        return Api.ok(modules.list());
    }

    @PostMapping("/modules")
    public ResponseEntity<String> createModule(@RequestBody String body) {
        return Api.created(modules.create(Json.read(body)));
    }

    @GetMapping("/modules/{id}")
    public ResponseEntity<String> module(@PathVariable String id) {
        return Api.ok(modules.view(id));
    }

    @PostMapping("/modules/{id}:start")
    public ResponseEntity<String> startModule(@PathVariable String id) {
        modules.start(id);
        return Api.ok(modules.view(id));
    }

    @PostMapping("/modules/{id}:stop")
    public ResponseEntity<String> stopModule(@PathVariable String id) {
        modules.stop(id);
        return Api.ok(modules.view(id));
    }

    @PostMapping("/modules/{id}:reload")
    public ResponseEntity<String> reloadModule(@PathVariable String id) {
        modules.reload(id);
        return Api.ok(modules.view(id));
    }

    @PostMapping("/modules/{id}:deliver-credential")
    public ResponseEntity<String> deliverCredential(@PathVariable String id,
                                                    @RequestBody(required = false) String body) {
        ModuleFacade.EndpointModule module = modules.endpointModule(id);
        JsonNode request = Json.read(body);
        return Api.ok(credentials.deliver(module.moduleId(), module.endpointId(), request.path("fields")));
    }

    @GetMapping("/audit")
    public ResponseEntity<String> audit() {
        return Api.ok(audit.recentView(100));
    }
}
