package io.foundation.kernel.plugin;

import io.foundation.api.ErrorCode;
import io.foundation.api.FoundationException;
import io.foundation.api.FoundationPlugin;
import io.foundation.kernel.manifest.ManifestLoader;
import io.foundation.kernel.manifest.PluginManifest;
import io.foundation.kernel.runtime.PluginSource;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * PF4J-backed plugin discovery. PF4J owns the plugin classloader; Foundation owns the manifest and
 * lifecycle. The entry class is instantiated by the kernel, not by PF4J, so plugins only depend on
 * {@code foundation-api}.
 */
public final class PluginCatalog implements PluginSource {

    private static final Logger log = LoggerFactory.getLogger(PluginCatalog.class);

    private final Path pluginsDir;
    private final DefaultPluginManager manager;
    private final Map<String, Loaded> loaded = new LinkedHashMap<>();

    public PluginCatalog(Path pluginsDir) throws IOException {
        this.pluginsDir = pluginsDir;
        Files.createDirectories(pluginsDir);
        this.manager = new DefaultPluginManager(pluginsDir);
        this.manager.loadPlugins();
        scan();
    }

    private void scan() {
        loaded.clear();
        for (PluginWrapper wrapper : manager.getPlugins()) {
            Path path = wrapper.getPluginPath();
            try {
                PluginManifest manifest = readManifest(path);
                if (manifest == null) {
                    log.warn("plugin artifact {} has no plugin.yaml, ignored", path.getFileName());
                    continue;
                }
                if (!manifest.id().equals(wrapper.getPluginId())) {
                    throw new FoundationException(ErrorCode.MANIFEST_INVALID,
                            "plugin.yaml id " + manifest.id() + " does not match plugin.properties id "
                                    + wrapper.getPluginId());
                }
                loaded.put(manifest.id(), new Loaded(manifest, path, stamp(path)));
                log.info("discovered plugin {} {} ({})", manifest.id(), manifest.version(), path.getFileName());
            } catch (RuntimeException | IOException failure) {
                log.error("cannot load plugin from {}: {}", path, failure.getMessage());
            }
        }
    }

    @Override
    public List<LoadedPlugin> plugins() {
        List<LoadedPlugin> view = new ArrayList<>();
        loaded.values().forEach(entry -> view.add(
                new LoadedPlugin(entry.manifest.id(), entry.manifest, entry.path.toString())));
        return view;
    }

    @Override
    public PluginManifest manifest(String pluginId) {
        Loaded entry = loaded.get(pluginId);
        return entry == null ? null : entry.manifest;
    }

    @Override
    public synchronized FoundationPlugin newInstance(String pluginId) {
        Loaded entry = loaded.get(pluginId);
        if (entry == null) {
            throw new FoundationException(ErrorCode.MANIFEST_INVALID, "plugin " + pluginId + " is not installed");
        }
        if (entry.manifest.runtime() == null || entry.manifest.runtime().entry() == null) {
            throw new FoundationException(ErrorCode.MANIFEST_INVALID,
                    "plugin " + pluginId + " has no runtime entry");
        }
        try {
            long current = stamp(entry.path);
            if (current != entry.stamp) {
                log.info("plugin artifact {} changed, reloading classloader", entry.path.getFileName());
                manager.unloadPlugin(pluginId);
                manager.loadPlugin(entry.path);
                entry = new Loaded(entry.manifest, entry.path, current);
                loaded.put(pluginId, entry);
            }
            ClassLoader classLoader = manager.getPluginClassLoader(pluginId);
            if (classLoader == null) {
                throw new FoundationException(ErrorCode.MANIFEST_INVALID,
                        "plugin " + pluginId + " has no classloader");
            }
            Class<?> type = Class.forName(entry.manifest.runtime().entry(), true, classLoader);
            Object instance = type.getDeclaredConstructor().newInstance();
            if (!(instance instanceof FoundationPlugin plugin)) {
                throw new FoundationException(ErrorCode.MANIFEST_INVALID,
                        entry.manifest.runtime().entry() + " does not implement io.foundation.api.FoundationPlugin");
            }
            return plugin;
        } catch (FoundationException failure) {
            throw failure;
        } catch (ReflectiveOperationException failure) {
            throw new FoundationException(ErrorCode.MANIFEST_INVALID,
                    "cannot instantiate plugin " + pluginId + ": " + failure, failure);
        }
    }

    public Path pluginsDir() {
        return pluginsDir;
    }

    private static long stamp(Path path) {
        try {
            if (Files.isDirectory(path)) {
                Path manifest = path.resolve("plugin.yaml");
                return Files.exists(manifest) ? Files.getLastModifiedTime(manifest).toMillis() : 0L;
            }
            return Files.getLastModifiedTime(path).toMillis() ^ Files.size(path);
        } catch (IOException ignored) {
            return 0L;
        }
    }

    private static PluginManifest readManifest(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Path manifest = path.resolve("plugin.yaml");
            if (!Files.exists(manifest)) {
                return null;
            }
            try (InputStream in = Files.newInputStream(manifest)) {
                return ManifestLoader.read(in, manifest.toString());
            }
        }
        try (JarFile jar = new JarFile(path.toFile())) {
            JarEntry entry = jar.getJarEntry("plugin.yaml");
            if (entry == null) {
                return null;
            }
            try (InputStream in = jar.getInputStream(entry)) {
                return ManifestLoader.read(in, path.getFileName() + "!/plugin.yaml");
            }
        }
    }

    private record Loaded(PluginManifest manifest, Path path, long stamp) {
    }
}
