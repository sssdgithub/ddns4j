package io.foundation.host.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class Json {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Json() {
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    public static ObjectNode object() {
        return MAPPER.createObjectNode();
    }

    public static ArrayNode array() {
        return MAPPER.createArrayNode();
    }

    public static JsonNode read(String body) {
        try {
            if (body == null || body.isBlank()) {
                return MAPPER.createObjectNode();
            }
            return MAPPER.readTree(body);
        } catch (Exception failure) {
            throw new ApiException(400, "BAD_REQUEST", "invalid JSON body");
        }
    }

    public static String write(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (Exception failure) {
            throw new IllegalStateException("cannot serialize response", failure);
        }
    }
}
