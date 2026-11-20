package io.foundation.kernel.runtime;

import io.foundation.api.Disposable;
import io.foundation.api.ErrorCode;
import io.foundation.api.FoundationException;
import io.foundation.api.FoundationPlugin;
import io.foundation.kernel.effect.EffectStack;
import io.foundation.kernel.http.ModuleHttpRouter;
import io.foundation.kernel.http.RouteRegistry;
import io.foundation.kernel.manifest.PluginManifest;
import io.foundation.kernel.service.HostServiceRegistry;
import io.foundation.kernel.service.ModuleServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Host-side module lifecycle: install / activate / stop / reload / uninstall.
 */
public final class ModuleManager {

    private static final Logger log = LoggerFactory.getLogger(ModuleManager.class);

    private final PluginSource plugins;
    private final HostServiceRegistry registry;
    private final RouteRegistry routes;
    private final Clock clock;
    private final Map<String, ModuleRecord> modules = new ConcurrentHashMap<>();
    private final Map<String, Object> locks = new ConcurrentHashMap<>();
    private final Set<String> cascading = ConcurrentHashMap.newKeySet();
    private final List<StateListener> listeners = new ArrayList<>();

    public interface StateListener {
        void onStateChange(ModuleRecord module, ModuleState from, ModuleState to, String reason);
    }

    public ModuleManager(PluginSource plugins, HostServiceRegistry registry, RouteRegistry routes, Clock clock) {
        this.plugins = plugins;
        this.registry = registry;
        this.routes = routes;
        this.clock = clock;
        registry.onUnavailable(this::onServiceUnavailable);
    }

    public void addListener(StateListener listener) {
        listeners.add(listener);
    }

    public ModuleRecord create(String pluginId, ModuleTarget target, Map<String, Object> config) {
        if (pluginId == null || pluginId.isBlank()) {
            throw new FoundationException(ErrorCode.MANIFEST_INVALID, "pluginId is required");
        }
        PluginManifest manifest = plugins.manifest(pluginId);
        if (manifest == null) {
            throw new FoundationException(ErrorCode.MANIFEST_INVALID, "plugin " + pluginId + " is not installed");
        }
        if (!target.isHost()) {
            throw new FoundationException(ErrorCode.CONFIG_INVALID,
                    "endpoint modules are records only in v1, they are not executed by the host");
        }
        String id = "mod_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        ModuleRecord record = new ModuleRecord(id, pluginId, target, config == null ? Map.of() : config);
        record.manifest(manifest);
        modules.put(id, record);
        return record;
    }

    public ModuleRecord get(String moduleId) {
        ModuleRecord record = modules.get(moduleId);
        if (record == null) {
            throw new FoundationException(ErrorCode.DEPENDENCY_MISSING, "module " + moduleId + " does not exist");
        }
        return record;
    }

    public List<ModuleRecord> list() {
        return modules.values().stream()
                .sorted(Comparator.comparing(ModuleRecord::createdAt))
                .toList();
    }

    public synchronized void start(String moduleId) {
        withLock(moduleId, () -> doStart(get(moduleId)));
    }

    public synchronized void stop(String moduleId) {
        withLock(moduleId, () -> doStop(get(moduleId), "stopped by request"));
    }

    public synchronized void reload(String moduleId) {
        withLock(moduleId, () -> {
            ModuleRecord record = get(moduleId);
            doStop(record, "reload");
            doStart(record);
            if (record.state() != ModuleState.ACTIVE) {
                throw new FoundationException(ErrorCode.CONFIG_INVALID,
                        "module " + moduleId + " failed to reload: " + record.lastError());
            }
        });
    }

    public synchronized void uninstall(String moduleId) {
        withLock(moduleId, () -> {
            ModuleRecord record = get(moduleId);
            if (record.state() == ModuleState.ACTIVE) {
                doStop(record, "uninstall");
            }
            transition(record, ModuleState.UNINSTALLED, "uninstalled");
            record.context(null);
            record.effects(null);
        });
    }

    private void doStart(ModuleRecord record) {
        if (record.state() == ModuleState.ACTIVE) {
            return;
        }
        PluginManifest manifest = plugins.manifest(record.pluginId());
        if (manifest == null) {
            throw new FoundationException(ErrorCode.MANIFEST_INVALID,
                    "plugin " + record.pluginId() + " disappeared");
        }
        record.manifest(manifest);
        if (!"server".equals(manifest.kind())) {
            record.state(ModuleState.FAILED);
            record.lastError("kind " + manifest.kind() + " cannot run as a host module");
            throw new FoundationException(ErrorCode.MANIFEST_INVALID, record.lastError());
        }
        List<String> missing = manifest.requires().stream()
                .filter(serviceId -> !registry.available(serviceId))
                .toList();
        if (!missing.isEmpty()) {
            transition(record, ModuleState.RESOLVED, "dependency missing: " + String.join(", ", missing));
            record.lastError("dependency missing: " + String.join(", ", missing));
            return;
        }

        EffectStack effects = new EffectStack(record.id());
        ModuleServiceRegistry serviceRegistry = new ModuleServiceRegistry(registry, record.id());
        ModuleContext context = new ModuleContext(record.pluginId(), record.id(), record.config(),
                serviceRegistry, null, effects, clock);
        ModuleHttpRouter router = new ModuleHttpRouter(routes, record.id(), context);
        context = new ModuleContext(record.pluginId(), record.id(), record.config(),
                serviceRegistry, router, effects, clock);
        record.context(context);
        record.effects(effects);
        int generation = record.nextGeneration();

        try {
            FoundationPlugin plugin = plugins.newInstance(record.pluginId());
            Disposable undo = plugin.install(context);
            if (undo != null) {
                effects.register("install-return", () -> undo);
            }
            registry.activate(record.id());
            record.lastError(null);
            record.startedAt(Instant.now(clock));
            transition(record, ModuleState.ACTIVE, "installed generation " + generation);
            log.info("module {} started (plugin {}, generation {})", record.id(), record.pluginId(), generation);
        } catch (Throwable failure) {
            effects.dispose();
            registry.removeOwner(record.id());
            record.context(null);
            record.lastError(failure.getClass().getSimpleName() + ": " + failure.getMessage());
            transition(record, ModuleState.FAILED, "install failed: " + record.lastError());
            log.error("module {} failed to start: {}", record.id(), failure.toString());
            if (failure instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new FoundationException(ErrorCode.CONFIG_INVALID,
                    "module " + record.id() + " failed to install", failure);
        }
    }

    private void doStop(ModuleRecord record, String reason) {
        if (record.state() != ModuleState.ACTIVE && record.state() != ModuleState.FAILED) {
            transition(record, ModuleState.STOPPED, reason);
            return;
        }
        registry.deactivate(record.id());
        EffectStack effects = record.effects();
        if (effects != null) {
            effects.dispose();
        }
        registry.removeOwner(record.id());
        record.context(null);
        record.startedAt(null);
        transition(record, ModuleState.STOPPED, reason);
        log.info("module {} stopped ({})", record.id(), reason);
    }

    private void onServiceUnavailable(String serviceId) {
        if (!cascading.add(serviceId)) {
            return;
        }
        try {
            List<ModuleRecord> dependents = modules.values().stream()
                    .filter(module -> module.state() == ModuleState.ACTIVE)
                    .filter(module -> module.manifest() != null
                            && module.manifest().requires().contains(serviceId))
                    .toList();
            for (ModuleRecord dependent : dependents) {
                log.info("module {} loses dependency {}, stopping", dependent.id(), serviceId);
                synchronized (this) {
                    if (dependent.state() == ModuleState.ACTIVE) {
                        doStop(dependent, "dependency lost: " + serviceId);
                    }
                }
            }
        } finally {
            cascading.remove(serviceId);
        }
    }

    private void transition(ModuleRecord record, ModuleState to, String reason) {
        ModuleState from = record.state();
        if (from == to) {
            return;
        }
        record.state(to);
        record.reason(reason);
        for (StateListener listener : listeners) {
            try {
                listener.onStateChange(record, from, to, reason);
            } catch (RuntimeException failure) {
                log.warn("state listener failed: {}", failure.toString());
            }
        }
    }

    private void withLock(String moduleId, Runnable action) {
        Object lock = locks.computeIfAbsent(moduleId, key -> new Object());
        synchronized (lock) {
            action.run();
        }
    }

    public Map<String, Object> summary(ModuleRecord record) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("moduleId", record.id());
        view.put("pluginId", record.pluginId());
        view.put("target", record.target().kind());
        view.put("state", record.state().name().toLowerCase(java.util.Locale.ROOT));
        view.put("reason", record.reason());
        view.put("lastError", record.lastError());
        view.put("config", record.config());
        view.put("effects", record.effects() == null ? null : record.effects().snapshot());
        return view;
    }
}
