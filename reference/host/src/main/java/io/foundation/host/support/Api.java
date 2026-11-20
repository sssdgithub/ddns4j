package io.foundation.host.support;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Unified envelope: { code, message, data, traceId }.
 */
public final class Api {

    private Api() {
    }

    public static ResponseEntity<String> ok(Object data) {
        return respond(200, "OK", "ok", data);
    }

    public static ResponseEntity<String> created(Object data) {
        return respond(201, "OK", "created", data);
    }

    public static ResponseEntity<String> accepted(Object data) {
        return respond(202, "OK", "accepted", data);
    }

    public static ResponseEntity<String> respond(int status, String code, String message, Object data) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("message", message);
        body.put("data", data);
        body.put("traceId", UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Json.write(body));
    }

    public static ResponseEntity<String> error(int status, String code, String message) {
        return respond(status, code, message, null);
    }
}
