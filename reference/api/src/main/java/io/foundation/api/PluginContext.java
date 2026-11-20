package io.foundation.api;

import io.foundation.api.http.HttpRouter;

import java.time.Clock;
import java.util.Map;

/**
 * Module-scoped context handed to a plugin at install time. All lookups must go through this object;
 * static singletons are forbidden by the contract.
 */
public interface PluginContext {

    String pluginId();

    String moduleId();

    /**
     * Module configuration (already merged with intercept chain and app defaults).
     */
    Map<String, Object> config();

    ServiceRegistry services();

    <T> T getService(String serviceId, Class<T> type);

    HttpRouter http();

    /**
     * Register a reversible side effect. The factory runs synchronously and must return a disposer.
     */
    Disposable effect(String label, EffectAction action);

    /**
     * Register a service owned by this module. Shorthand for {@code services().provide(...)} wrapped
     * in an effect so it is always rolled back.
     */
    <T> Disposable provide(String serviceId, Class<T> type, T instance);

    /**
     * Snapshot of the current effect tree (empty after full rollback).
     */
    EffectNode getEffects();

    Clock clock();
}
