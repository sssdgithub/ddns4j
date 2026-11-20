package io.foundation.kernel.manifest;

import java.util.List;
import java.util.Map;

/**
 * Parsed {@code plugin.yaml}. Unknown fields are ignored (forward compatible).
 */
public record PluginManifest(
        String apiVersion,
        String id,
        String name,
        String version,
        String kind,
        RuntimeSpec runtime,
        List<String> provides,
        List<String> requires,
        List<Map<String, Object>> capabilities,
        String configSchema,
        List<String> profiles,
        List<String> modules,
        List<String> pages,
        Map<String, Object> compatibility) {

    public record RuntimeSpec(String type, String entry) {
    }

    public boolean isApp() {
        return "app".equals(kind);
    }
}
