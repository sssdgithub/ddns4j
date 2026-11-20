package io.foundation.kernel.http;

import io.foundation.api.Disposable;
import io.foundation.api.ErrorCode;
import io.foundation.api.FoundationException;
import io.foundation.api.http.RouteHandler;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Host-owned HTTP routes contributed by modules. A route disappears when its disposing handle runs.
 */
public final class RouteRegistry {

    public record Route(String method, String path, String moduleId, RouteHandler handler) {
    }

    private final Map<String, Route> routes = new ConcurrentHashMap<>();
    private final Map<String, String> tombstones = new ConcurrentHashMap<>();

    public Disposable register(String method, String path, String moduleId, RouteHandler handler) {
        if (method == null || path == null || handler == null) {
            throw new IllegalArgumentException("method, path and handler are required");
        }
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("route path must start with /: " + path);
        }
        String key = key(method, path);
        Route route = new Route(method.toUpperCase(Locale.ROOT), path, moduleId, handler);
        Route previous = routes.putIfAbsent(key, route);
        if (previous != null) {
            throw new FoundationException(ErrorCode.SERVICE_CONFLICT,
                    "route " + key + " already registered by " + previous.moduleId());
        }
        tombstones.remove(key);
        return () -> {
            if (routes.remove(key, route)) {
                tombstones.put(key, moduleId);
            }
        };
    }

    public Optional<Route> match(String method, String path) {
        return Optional.ofNullable(routes.get(key(method, path)));
    }

    /**
     * Module that used to own a route which is now gone. Used to answer 503 MODULE_INACTIVE instead
     * of a plain 404 after a module is stopped.
     */
    public Optional<String> tombstone(String method, String path) {
        return Optional.ofNullable(tombstones.get(key(method, path)));
    }

    public List<Route> routes() {
        return routes.values().stream()
                .sorted((a, b) -> key(a.method(), a.path()).compareTo(key(b.method(), b.path())))
                .toList();
    }

    private static String key(String method, String path) {
        return method.toUpperCase(Locale.ROOT) + " " + path;
    }
}
