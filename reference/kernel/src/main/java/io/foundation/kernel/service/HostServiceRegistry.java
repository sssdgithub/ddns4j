package io.foundation.kernel.service;

import io.foundation.api.Disposable;
import io.foundation.api.ErrorCode;
import io.foundation.api.FoundationException;
import io.foundation.api.ServiceRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Process-wide service registry. Each entry remembers its owner module; entries owned by a module
 * become resolvable only while that module is ACTIVE.
 */
public final class HostServiceRegistry implements ServiceRegistry {

    public static final String HOST_OWNER = "host";

    private final Map<String, Entry> entries = new ConcurrentHashMap<>();
    private final List<Consumer<String>> unavailableListeners = new CopyOnWriteArrayList<>();

    public HostServiceRegistry() {
    }

    public void onUnavailable(Consumer<String> listener) {
        unavailableListeners.add(listener);
    }

    @Override
    public <T> T get(String serviceId, Class<T> type, boolean strict) {
        Entry entry = entries.get(serviceId);
        T value = entry == null ? null : entry.currentValue(type);
        if (value == null) {
            if (strict) {
                throw new FoundationException(ErrorCode.DEPENDENCY_MISSING,
                        "service " + serviceId + " is not available");
            }
            return null;
        }
        return value;
    }

    @Override
    public <T> Disposable provide(String serviceId, Class<T> type, T instance) {
        throw new FoundationException(ErrorCode.SERVICE_FORBIDDEN,
                "provide() must be called through a module context");
    }

    public <T> Disposable provide(String serviceId, Class<T> type, T instance, String ownerId) {
        if (instance == null) {
            throw new IllegalArgumentException("service " + serviceId + " instance must not be null");
        }
        Entry entry = new Entry(serviceId, ownerId, instance, null, null);
        Entry previous = entries.putIfAbsent(serviceId, entry);
        if (previous != null) {
            throw new FoundationException(ErrorCode.SERVICE_CONFLICT,
                    "service " + serviceId + " already provided by " + previous.ownerId);
        }
        return () -> removeEntry(serviceId, ownerId, entry);
    }

    @Override
    public <T> Disposable accessor(String serviceId, Class<T> type, Supplier<T> getter, Consumer<T> setter) {
        throw new FoundationException(ErrorCode.SERVICE_FORBIDDEN,
                "accessor() must be called through a module context");
    }

    public <T> Disposable accessor(String serviceId, Class<T> type, Supplier<T> getter,
                                   Consumer<T> setter, String ownerId) {
        Entry entry = new Entry(serviceId, ownerId, null, getter, setter);
        Entry previous = entries.putIfAbsent(serviceId, entry);
        if (previous != null) {
            throw new FoundationException(ErrorCode.SERVICE_CONFLICT,
                    "service " + serviceId + " already provided by " + previous.ownerId);
        }
        return () -> removeEntry(serviceId, ownerId, entry);
    }

    @Override
    public void set(String serviceId, Object value) {
        throw new FoundationException(ErrorCode.SERVICE_FORBIDDEN,
                "set() must be called through a module context");
    }

    public void set(String serviceId, Object value, String ownerId) {
        Entry entry = entries.get(serviceId);
        if (entry == null) {
            throw new FoundationException(ErrorCode.DEPENDENCY_MISSING, "service " + serviceId + " does not exist");
        }
        if (!entry.ownerId.equals(ownerId)) {
            throw new FoundationException(ErrorCode.SERVICE_FORBIDDEN,
                    "service " + serviceId + " is owned by " + entry.ownerId);
        }
        entry.update(value);
    }

    @Override
    public boolean available(String serviceId) {
        Entry entry = entries.get(serviceId);
        return entry != null && entry.active;
    }

    /**
     * Register a host-owned service that is always active.
     */
    public void hostProvide(String serviceId, Object instance) {
        Entry entry = new Entry(serviceId, HOST_OWNER, instance, null, null);
        entry.active = true;
        Entry previous = entries.putIfAbsent(serviceId, entry);
        if (previous != null) {
            throw new FoundationException(ErrorCode.SERVICE_CONFLICT,
                    "service " + serviceId + " already provided by " + previous.ownerId);
        }
    }

    public void activate(String ownerId) {
        entries.values().stream()
                .filter(entry -> entry.ownerId.equals(ownerId))
                .forEach(entry -> entry.active = true);
    }

    public void deactivate(String ownerId) {
        List<String> released = new ArrayList<>();
        for (Entry entry : entries.values()) {
            if (entry.ownerId.equals(ownerId) && entry.active) {
                entry.active = false;
                released.add(entry.serviceId);
            }
        }
        released.forEach(this::fireUnavailable);
    }

    public void removeOwner(String ownerId) {
        List<String> removed = new ArrayList<>();
        for (Entry entry : entries.values()) {
            if (entry.ownerId.equals(ownerId)) {
                if (entries.remove(entry.serviceId, entry)) {
                    removed.add(entry.serviceId);
                }
            }
        }
        removed.forEach(this::fireUnavailable);
    }

    public Map<String, String> owners() {
        Map<String, String> view = new java.util.TreeMap<>();
        entries.forEach((id, entry) -> view.put(id, entry.ownerId));
        return view;
    }

    private void removeEntry(String serviceId, String ownerId, Entry entry) {
        if (entries.remove(serviceId, entry)) {
            fireUnavailable(serviceId);
        }
    }

    private void fireUnavailable(String serviceId) {
        for (Consumer<String> listener : unavailableListeners) {
            try {
                listener.accept(serviceId);
            } catch (RuntimeException ignored) {
                // listeners must not break the registry
            }
        }
    }

    private static final class Entry {
        private final String serviceId;
        private final String ownerId;
        private final Supplier<?> getter;
        private final Consumer<Object> setter;
        private volatile Object instance;
        private volatile boolean active;

        @SuppressWarnings("unchecked")
        private Entry(String serviceId, String ownerId, Object instance,
                      Supplier<?> getter, Consumer<?> setter) {
            this.serviceId = serviceId;
            this.ownerId = ownerId;
            this.instance = instance;
            this.getter = getter;
            this.setter = (Consumer<Object>) setter;
        }

        private <T> T currentValue(Class<T> type) {
            if (!active) {
                return null;
            }
            Object value = getter != null ? getter.get() : instance;
            if (value == null) {
                return null;
            }
            if (!type.isInstance(value)) {
                throw new FoundationException(ErrorCode.DEPENDENCY_MISSING,
                        "service " + serviceId + " is not a " + type.getName());
            }
            return type.cast(value);
        }

        private void update(Object value) {
            if (setter != null) {
                setter.accept(value);
            }
            this.instance = value;
        }
    }
}
