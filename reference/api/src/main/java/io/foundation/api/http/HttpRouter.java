package io.foundation.api.http;

import io.foundation.api.Disposable;

/**
 * Host-provided HTTP router. Plugins register routes through an effect so stopping the module makes
 * the route disappear.
 */
public interface HttpRouter {

    Disposable route(String method, String path, RouteHandler handler);
}
