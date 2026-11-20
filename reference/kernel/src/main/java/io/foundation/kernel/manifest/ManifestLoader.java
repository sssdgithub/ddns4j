package io.foundation.kernel.manifest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.foundation.api.ErrorCode;
import io.foundation.api.FoundationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public final class ManifestLoader {

    public static final String CONTRACT_VERSION = "foundation/v1";

    private static final ObjectMapper YAML = new ObjectMapper(new YAMLFactory());

    private ManifestLoader() {
    }

    @SuppressWarnings("unchecked")
    public static PluginManifest read(InputStream in, String source) {
        try {
            Map<String, Object> raw = YAML.readValue(in, Map.class);
            return fromMap(raw, source);
        } catch (IOException e) {
            throw new FoundationException(ErrorCode.MANIFEST_INVALID, "cannot parse manifest " + source, e);
        }
    }

    public static PluginManifest fromMap(Map<String, Object> raw, String source) {
        String apiVersion = str(raw.get("apiVersion"));
        if (apiVersion == null || !apiVersion.startsWith("foundation/v")) {
            throw new FoundationException(ErrorCode.API_VERSION_MISMATCH,
                    "manifest " + source + " has unsupported apiVersion " + apiVersion);
        }
        if (!"v1".equals(apiVersion.substring("foundation/".length()))) {
            throw new FoundationException(ErrorCode.API_VERSION_MISMATCH,
                    "manifest " + source + " requires contract " + apiVersion + ", kernel speaks " + CONTRACT_VERSION);
        }
        String id = str(raw.get("id"));
        String version = str(raw.get("version"));
        String kind = str(raw.get("kind"));
        if (id == null || id.isBlank()) {
            throw new FoundationException(ErrorCode.MANIFEST_INVALID, "manifest " + source + " misses id");
        }
        if (version == null || version.isBlank()) {
            throw new FoundationException(ErrorCode.MANIFEST_INVALID, "manifest " + source + " misses version");
        }
        if (kind == null || !List.of("server", "endpoint", "app").contains(kind)) {
            throw new FoundationException(ErrorCode.MANIFEST_INVALID,
                    "manifest " + source + " has invalid kind " + kind);
        }

        PluginManifest.RuntimeSpec runtime = null;
        Object runtimeRaw = raw.get("runtime");
        if (runtimeRaw instanceof Map<?, ?> map) {
            runtime = new PluginManifest.RuntimeSpec(str(map.get("type")), str(map.get("entry")));
        }
        if (!"app".equals(kind) && (runtime == null || runtime.entry() == null)) {
            throw new FoundationException(ErrorCode.MANIFEST_INVALID,
                    "manifest " + source + " requires runtime.entry for kind=" + kind);
        }

        return new PluginManifest(
                apiVersion,
                id,
                str(raw.getOrDefault("name", id)),
                version,
                kind,
                runtime,
                strList(raw.get("provides")),
                strList(raw.get("requires")),
                mapList(raw.get("capabilities")),
                str(raw.get("configSchema")),
                strList(raw.get("profiles")),
                strList(raw.get("modules")),
                strList(raw.get("pages")),
                map(raw.get("compatibility")));
    }

    private static String str(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        return value instanceof Map<?, ?> m ? (Map<String, Object>) m : Map.of();
    }

    @SuppressWarnings("unchecked")
    private static List<String> strList(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> mapList(Object value) {
        if (value instanceof List<?> list) {
            return list.stream()
                    .filter(Map.class::isInstance)
                    .map(item -> (Map<String, Object>) item)
                    .toList();
        }
        return List.of();
    }
}
