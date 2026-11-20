package io.foundation.api.http;

import java.util.Map;

public record HttpRequest(String method, String path, String query, Map<String, String> headers, String body) {

    public HttpRequest {
        headers = headers == null ? Map.of() : Map.copyOf(headers);
    }

    public String header(String name) {
        return headers.get(name);
    }
}
