package io.foundation.kernel.http;

import io.foundation.api.Disposable;
import io.foundation.api.http.HttpRouter;
import io.foundation.api.http.RouteHandler;

/**
 * Router handed to plugins. Every registration becomes an effect so module stop removes the route.
 */
public final class ModuleHttpRouter implements HttpRouter {

    private final RouteRegistry routes;
    private final String moduleId;
    private final io.foundation.api.PluginContext context;

    public ModuleHttpRouter(RouteRegistry routes, String moduleId, io.foundation.api.PluginContext context) {
        this.routes = routes;
        this.moduleId = moduleId;
        this.context = context;
    }

    @Override
    public Disposable route(String method, String path, RouteHandler handler) {
        return context.effect("http:" + method.toUpperCase(java.util.Locale.ROOT) + " " + path,
                () -> routes.register(method, path, moduleId, handler));
    }
}
