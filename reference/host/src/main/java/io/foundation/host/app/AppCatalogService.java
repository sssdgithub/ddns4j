package io.foundation.host.app;

import io.foundation.host.config.FoundationProperties;
import io.foundation.kernel.manifest.ManifestLoader;
import io.foundation.kernel.manifest.PluginManifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Reads app bundles (kind=app manifests) from the configured apps directory. v1 never executes
 * apps on the host; it only exposes their declared modules and pages.
 */
@Service
public class AppCatalogService {

    private static final Logger log = LoggerFactory.getLogger(AppCatalogService.class);

    private final FoundationProperties properties;

    public AppCatalogService(FoundationProperties properties) {
        this.properties = properties;
    }

    public List<Map<String, Object>> list() {
        List<Map<String, Object>> apps = new ArrayList<>();
        Path dir = properties.getAppsDir();
        if (!Files.isDirectory(dir)) {
            return apps;
        }
        try (Stream<Path> files = Files.list(dir)) {
            files.filter(path -> path.getFileName().toString().endsWith(".yaml")
                            || path.getFileName().toString().endsWith(".yml"))
                    .sorted()
                    .forEach(path -> {
                        try (InputStream in = Files.newInputStream(path)) {
                            PluginManifest manifest = ManifestLoader.read(in, path.toString());
                            if (!manifest.isApp()) {
                                return;
                            }
                            Map<String, Object> view = new LinkedHashMap<>();
                            view.put("id", manifest.id());
                            view.put("name", manifest.name());
                            view.put("version", manifest.version());
                            view.put("modules", manifest.modules());
                            view.put("pages", manifest.pages());
                            view.put("source", path.getFileName().toString());
                            apps.add(view);
                        } catch (RuntimeException | java.io.IOException failure) {
                            log.warn("cannot read app bundle {}: {}", path, failure.getMessage());
                        }
                    });
        } catch (java.io.IOException failure) {
            log.warn("cannot list app bundles in {}: {}", dir, failure.getMessage());
        }
        return apps;
    }
}
