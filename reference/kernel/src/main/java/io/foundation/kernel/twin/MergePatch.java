package io.foundation.kernel.twin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * RFC 7396 JSON Merge Patch. Objects merge recursively, arrays are replaced wholesale, null removes.
 */
public final class MergePatch {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private MergePatch() {
    }

    public static JsonNode apply(JsonNode target, JsonNode patch) {
        if (patch == null) {
            return target == null ? MAPPER.createObjectNode() : target;
        }
        if (!patch.isObject()) {
            return patch;
        }
        ObjectNode result = target != null && target.isObject()
                ? ((ObjectNode) target).deepCopy()
                : MAPPER.createObjectNode();
        patch.fields().forEachRemaining(field -> {
            JsonNode value = field.getValue();
            if (value.isNull()) {
                result.remove(field.getKey());
            } else {
                result.set(field.getKey(), apply(result.get(field.getKey()), value));
            }
        });
        return result;
    }

    public static ObjectNode emptyObject() {
        return MAPPER.createObjectNode();
    }
}
