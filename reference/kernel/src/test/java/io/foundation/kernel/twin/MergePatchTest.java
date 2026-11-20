package io.foundation.kernel.twin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MergePatchTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void mergesObjectsRecursively() throws Exception {
        JsonNode target = mapper.readTree("{\"a\":1,\"b\":{\"x\":1,\"y\":2},\"c\":[1,2]}");
        JsonNode patch = mapper.readTree("{\"b\":{\"y\":3,\"z\":4},\"c\":[9]}");
        JsonNode result = MergePatch.apply(target, patch);
        assertThat(result.get("a").asInt()).isEqualTo(1);
        assertThat(result.get("b").get("x").asInt()).isEqualTo(1);
        assertThat(result.get("b").get("y").asInt()).isEqualTo(3);
        assertThat(result.get("b").get("z").asInt()).isEqualTo(4);
        assertThat(result.get("c").isArray()).isTrue();
        assertThat(result.get("c")).hasSize(1);
        assertThat(result.get("c").get(0).asInt()).isEqualTo(9);
    }

    @Test
    void nullRemovesKeyAndArraysInsideModulesAreReplaced() throws Exception {
        JsonNode target = mapper.readTree("{\"config\":{\"a\":1,\"b\":2},\"modules\":[{\"id\":\"m1\"}]}");
        JsonNode patch = mapper.readTree("{\"config\":{\"b\":null},\"modules\":[{\"id\":\"m2\"}]}");
        JsonNode result = MergePatch.apply(target, patch);
        assertThat(result.get("config").has("b")).isFalse();
        assertThat(result.get("config").get("a").asInt()).isEqualTo(1);
        assertThat(result.get("modules")).hasSize(1);
        assertThat(result.get("modules").get(0).get("id").asText()).isEqualTo("m2");
    }

    @Test
    void scalarPatchReplacesTarget() throws Exception {
        JsonNode target = mapper.readTree("{\"a\":1}");
        JsonNode result = MergePatch.apply(target, mapper.readTree("\"text\""));
        assertThat(result.asText()).isEqualTo("text");
    }
}
