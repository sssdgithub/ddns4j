package io.foundation.kernel.service;

import io.foundation.api.Disposable;
import io.foundation.api.ServiceRegistry;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Module-scoped view over the host registry. Ownership is pinned to this module.
 */
public final class ModuleServiceRegistry implements ServiceRegistry {

    private final HostServiceRegistry host;
    private final String ownerId;

    public ModuleServiceRegistry(HostServiceRegistry host, String ownerId) {
        this.host = host;
        this.ownerId = ownerId;
    }

    @Override
    public <T> T get(String serviceId, Class<T> type, boolean strict) {
        return host.get(serviceId, type, strict);
    }

    @Override
    public <T> Disposable provide(String serviceId, Class<T> type, T instance) {
        return host.provide(serviceId, type, instance, ownerId);
    }

    @Override
    public void set(String serviceId, Object value) {
        host.set(serviceId, value, ownerId);
    }

    @Override
    public <T> Disposable accessor(String serviceId, Class<T> type, Supplier<T> getter, Consumer<T> setter) {
        return host.accessor(serviceId, type, getter, setter, ownerId);
    }

    @Override
    public boolean available(String serviceId) {
        return host.available(serviceId);
    }
}
