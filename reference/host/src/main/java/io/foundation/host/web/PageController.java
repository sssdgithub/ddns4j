package io.foundation.host.web;

import com.fasterxml.jackson.databind.JsonNode;
import io.foundation.host.app.AppCatalogService;
import io.foundation.host.audit.AuditService;
import io.foundation.host.config.FoundationProperties;
import io.foundation.host.endpoint.EndpointRecord;
import io.foundation.host.endpoint.EndpointRegistry;
import io.foundation.host.endpoint.EndpointService;
import io.foundation.host.module.ModuleFacade;
import io.foundation.host.support.Json;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class PageController {

    private final FoundationProperties properties;
    private final EndpointRegistry endpointRegistry;
    private final EndpointService endpoints;
    private final ModuleFacade modules;
    private final AuditService audit;
    private final AppCatalogService apps;

    public PageController(FoundationProperties properties, EndpointRegistry endpointRegistry,
                          EndpointService endpoints, ModuleFacade modules, AuditService audit,
                          AppCatalogService apps) {
        this.properties = properties;
        this.endpointRegistry = endpointRegistry;
        this.endpoints = endpoints;
        this.modules = modules;
        this.audit = audit;
        this.apps = apps;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username, @RequestParam String password,
                          HttpSession session, Model model) {
        if (properties.getAdminUser().equals(username) && properties.getAdminPassword().equals(password)) {
            session.setAttribute(PageAuthInterceptor.SESSION_ADMIN, true);
            return "redirect:/endpoints";
        }
        model.addAttribute("error", "用户名或密码错误");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/endpoints";
    }

    @GetMapping("/endpoints")
    public String endpoints(Model model) {
        model.addAttribute("endpoints", endpoints.list());
        model.addAttribute("apps", apps.list());
        return "endpoints";
    }

    @GetMapping("/endpoints/{id}")
    public String endpoint(@PathVariable String id, Model model) {
        EndpointRecord record = endpointRegistry.require(id);
        Map<String, Object> view = endpoints.view(record);
        model.addAttribute("endpoint", view);
        model.addAttribute("desiredJson", pretty(record.desired()));
        model.addAttribute("reportedJson", pretty(record.reported()));
        return "endpoint-detail";
    }

    @PostMapping("/endpoints/{id}/twin")
    public String patchTwin(@PathVariable String id,
                            @RequestParam String revision,
                            @RequestParam(required = false) String config,
                            Model model) {
        try {
            com.fasterxml.jackson.databind.node.ObjectNode patch = Json.object();
            patch.set("config", Json.read(config));
            endpoints.patchTwin(id, revision, patch);
        } catch (RuntimeException failure) {
            model.addAttribute("error", failure.getMessage());
            return endpoint(id, model);
        }
        return "redirect:/endpoints/" + id;
    }

    @GetMapping("/modules")
    public String modules(Model model) {
        model.addAttribute("modules", modules.list());
        return "modules";
    }

    @PostMapping("/modules/{id}/start")
    public String start(@PathVariable String id) {
        modules.start(id);
        return "redirect:/modules";
    }

    @PostMapping("/modules/{id}/stop")
    public String stop(@PathVariable String id) {
        modules.stop(id);
        return "redirect:/modules";
    }

    @PostMapping("/modules/{id}/reload")
    public String reload(@PathVariable String id) {
        modules.reload(id);
        return "redirect:/modules";
    }

    @GetMapping("/audit")
    public String audit(Model model) {
        model.addAttribute("records", audit.recentView(200));
        return "audit";
    }

    private static String pretty(JsonNode node) {
        try {
            return Json.mapper().writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (Exception failure) {
            return String.valueOf(node);
        }
    }
}
