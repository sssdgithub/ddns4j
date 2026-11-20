package io.foundation.host.web;

import io.foundation.api.http.HttpRequest;
import io.foundation.api.http.HttpResponse;
import io.foundation.kernel.http.RouteRegistry;
import io.foundation.host.support.Json;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Dispatches requests to plugin-contributed routes before Spring MVC sees them.
 */
@Component
public class RouteDispatchFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RouteDispatchFilter.class);

    private final RouteRegistry routes;

    public RouteDispatchFilter(RouteRegistry routes) {
        this.routes = routes;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        Optional<RouteRegistry.Route> match = routes.match(request.getMethod(), path);
        if (match.isEmpty()) {
            Optional<String> tombstone = routes.tombstone(request.getMethod(), path);
            if (tombstone.isPresent()) {
                response.setStatus(503);
                response.setContentType("application/json");
                Map<String, Object> error = new LinkedHashMap<>();
                error.put("code", "MODULE_INACTIVE");
                error.put("message", "module " + tombstone.get() + " is not active");
                response.getWriter().write(Json.write(error));
                return;
            }
            chain.doFilter(request, response);
            return;
        }
        try {
            Map<String, String> headers = new LinkedHashMap<>();
            request.getHeaderNames().asIterator().forEachRemaining(name ->
                    headers.put(name.toLowerCase(), request.getHeader(name)));
            String body = request.getReader().lines().collect(Collectors.joining("\n"));
            HttpResponse result = match.get().handler().handle(
                    new HttpRequest(request.getMethod(), path, request.getQueryString(), headers, body));
            response.setStatus(result.status());
            response.setContentType(result.contentType() == null ? "application/json" : result.contentType());
            result.headers().forEach(response::setHeader);
            response.getWriter().write(result.body() == null ? "" : result.body());
        } catch (Exception failure) {
            log.error("plugin route {} {} failed: {}", request.getMethod(), path, failure.toString());
            response.setStatus(500);
            response.setContentType("application/json");
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("code", "PLUGIN_ERROR");
            error.put("message", failure.getMessage());
            response.getWriter().write(Json.write(error));
        }
    }
}
