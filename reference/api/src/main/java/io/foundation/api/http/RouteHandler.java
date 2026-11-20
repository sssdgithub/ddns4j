package io.foundation.api.http;

@FunctionalInterface
public interface RouteHandler {

    HttpResponse handle(HttpRequest request) throws Exception;
}
