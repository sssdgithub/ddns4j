package io.foundation.api;

/**
 * In-process service registry. Services are visible only inside one process and one scope.
 */
public interface ServiceRegistry {

    /**
     * Resolve a service. Returns {@code null} when unavailable unless {@code strict} is true,
     * in which case a {@link FoundationException} with code {@code DEPENDENCY_MISSING} is thrown.
     */
    <T> T get(String serviceId, Class<T> type, boolean strict);

    default <T> T get(String serviceId, Class<T> type) {
        return get(serviceId, type, true);
    }

    /**
     * Register a service owned by the calling module. The service becomes resolvable once the
     * owning module is ACTIVE and disappears when the module is stopped.
     */
    <T> Disposable provide(String serviceId, Class<T> type, T instance);

    /**
     * Replace the value of an existing service. Only the owner may call it.
     */
    void set(String serviceId, Object value);

    /**
     * Register a computed service (get/set accessors) owned by the calling module.
     */
    <T> Disposable accessor(String serviceId, Class<T> type, java.util.function.Supplier<T> getter,
                            java.util.function.Consumer<T> setter);

    /**
     * @return true when the service can currently be resolved by a strict lookup.
     */
    boolean available(String serviceId);
}
