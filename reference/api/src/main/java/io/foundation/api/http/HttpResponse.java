package io.foundation.api.http;

import java.util.Map;

public record HttpResponse(int status, String contentType, String body, Map<String, String> headers) {

    public HttpResponse {
        headers = headers == null ? Map.of() : Map.copyOf(headers);
    }

    public static HttpResponse json(int status, String body) {
        return new HttpResponse(status, "application/json", body, Map.of());
    }

    public static HttpResponse text(int status, String body) {
        return new HttpResponse(status, "text/plain; charset=utf-8", body, Map.of());
    }

    public static HttpResponse ok(String body) {
        return text(200, body);
    }
}
