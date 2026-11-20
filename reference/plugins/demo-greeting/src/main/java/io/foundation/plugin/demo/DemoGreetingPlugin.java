package io.foundation.plugin.demo;

import io.foundation.api.Disposable;
import io.foundation.api.FoundationPlugin;
import io.foundation.api.PluginContext;
import io.foundation.api.http.HttpResponse;

/**
 * Minimal server plugin: registers one route and one service. Everything is registered through
 * effects, so stopping the module leaves no route, service or thread behind.
 */
public final class DemoGreetingPlugin implements FoundationPlugin {

    public static final String SERVICE_ID = "demo.greeting";

    @Override
    public String id() {
        return "foundation.demo.greeting";
    }

    @Override
    public Disposable install(PluginContext context) {
        String greeting = String.valueOf(context.config().getOrDefault("greeting", "hello"));
        context.provide(SERVICE_ID, Greeting.class, new Greeting(greeting));

        context.http().route("GET", "/api/v1/demo/greeting", request -> HttpResponse.json(200,
                "{\"greeting\":\"" + escape(greeting) + "\",\"moduleId\":\"" + context.moduleId() + "\"}"));

        return Disposable.noop();
    }

    public record Greeting(String text) {
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
