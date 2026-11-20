package io.foundation.kernel.runtime;

import io.foundation.kernel.effect.EffectStack;
import io.foundation.kernel.manifest.PluginManifest;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * One deployment unit. Mutable state is guarded by {@link ModuleManager}.
 */
public final class ModuleRecord {

    private final String id;
    private final String pluginId;
    private final ModuleTarget target;
    private final Map<String, Object> config;
    private final Instant createdAt = Instant.now();
    private final AtomicInteger generation = new AtomicInteger();

    private volatile ModuleState state = ModuleState.RESOLVED;
    private volatile String lastError;
    private volatile String reason;
    private volatile Instant startedAt;
    private volatile ModuleContext context;
    private volatile EffectStack effects;
    private volatile PluginManifest manifest;

    public ModuleRecord(String id, String pluginId, ModuleTarget target, Map<String, Object> config) {
        this.id = id;
        this.pluginId = pluginId;
        this.target = target;
        this.config = new LinkedHashMap<>(config);
    }

    public String id() {
        return id;
    }

    public String pluginId() {
        return pluginId;
    }

    public ModuleTarget target() {
        return target;
    }

    public Map<String, Object> config() {
        return java.util.Collections.unmodifiableMap(config);
    }

    public ModuleState state() {
        return state;
    }

    public void state(ModuleState state) {
        this.state = state;
    }

    public String lastError() {
        return lastError;
    }

    public void lastError(String lastError) {
        this.lastError = lastError;
    }

    public String reason() {
        return reason;
    }

    public void reason(String reason) {
        this.reason = reason;
    }

    public Instant startedAt() {
        return startedAt;
    }

    public void startedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public ModuleContext context() {
        return context;
    }

    public void context(ModuleContext context) {
        this.context = context;
    }

    public EffectStack effects() {
        return effects;
    }

    public void effects(EffectStack effects) {
        this.effects = effects;
    }

    public PluginManifest manifest() {
        return manifest;
    }

    public void manifest(PluginManifest manifest) {
        this.manifest = manifest;
    }

    public int nextGeneration() {
        return generation.incrementAndGet();
    }

    public Instant createdAt() {
        return createdAt;
    }
}
