package io.foundation.kernel.runtime;

import io.foundation.api.Disposable;
import io.foundation.api.EffectAction;
import io.foundation.api.EffectNode;
import io.foundation.api.PluginContext;
import io.foundation.api.http.HttpRouter;
import io.foundation.kernel.effect.EffectStack;
import io.foundation.kernel.service.ModuleServiceRegistry;

import java.time.Clock;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Concrete {@link PluginContext} for one module generation.
 */
public final class ModuleContext implements PluginContext {

    private final String pluginId;
    private final String moduleId;
    private final Map<String, Object> config;
    private final ModuleServiceRegistry services;
    private final HttpRouter http;
    private final EffectStack effects;
    private final Clock clock;

    public ModuleContext(String pluginId, String moduleId, Map<String, Object> config,
                         ModuleServiceRegistry services, HttpRouter http, EffectStack effects, Clock clock) {
        this.pluginId = pluginId;
        this.moduleId = moduleId;
        this.config = Collections.unmodifiableMap(new LinkedHashMap<>(config));
        this.services = services;
        this.http = http;
        this.effects = effects;
        this.clock = clock;
    }

    @Override
    public String pluginId() {
        return pluginId;
    }

    @Override
    public String moduleId() {
        return moduleId;
    }

    @Override
    public Map<String, Object> config() {
        return config;
    }

    @Override
    public io.foundation.api.ServiceRegistry services() {
        return services;
    }

    @Override
    public <T> T getService(String serviceId, Class<T> type) {
        return services.get(serviceId, type, true);
    }

    @Override
    public HttpRouter http() {
        return http;
    }

    @Override
    public Disposable effect(String label, EffectAction action) {
        return effects.register(label, action);
    }

    @Override
    public <T> Disposable provide(String serviceId, Class<T> type, T instance) {
        return effects.register("service:" + serviceId, () -> services.provide(serviceId, type, instance));
    }

    @Override
    public EffectNode getEffects() {
        return effects.snapshot();
    }

    @Override
    public Clock clock() {
        return clock;
    }

    public EffectStack effectStack() {
        return effects;
    }
}
