package io.foundation.host.config;

import io.foundation.host.audit.AuditService;
import io.foundation.kernel.http.RouteRegistry;
import io.foundation.kernel.plugin.PluginCatalog;
import io.foundation.kernel.runtime.ModuleManager;
import io.foundation.kernel.service.HostServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Clock;

@Configuration
public class FoundationConfig {

    private static final Logger log = LoggerFactory.getLogger(FoundationConfig.class);

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public HostServiceRegistry serviceRegistry() {
        return new HostServiceRegistry();
    }

    @Bean
    public RouteRegistry routeRegistry() {
        return new RouteRegistry();
    }

    @Bean
    public PluginCatalog pluginCatalog(FoundationProperties properties, HostServiceRegistry services,
                                       RouteRegistry routes, Clock clock) throws IOException {
        PluginCatalog catalog = new PluginCatalog(properties.getPluginsDir());
        services.hostProvide("foundation.http.router", routes);
        services.hostProvide("foundation.clock", clock);
        return catalog;
    }

    @Bean
    public ModuleManager moduleManager(PluginCatalog catalog, HostServiceRegistry services, RouteRegistry routes,
                                       Clock clock, AuditService audit) {
        ModuleManager manager = new ModuleManager(catalog, services, routes, clock);
        manager.addListener((module, from, to, reason) -> audit.record("system", "module.state",
                module.id(), from + "->" + to + (reason == null ? "" : " (" + reason + ")")));
        return manager;
    }
}
