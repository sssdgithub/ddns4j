package io.foundation.kernel.runtime;

import io.foundation.api.FoundationPlugin;
import io.foundation.kernel.manifest.PluginManifest;

import java.util.List;

/**
 * Source of plugin manifests and fresh plugin instances. Implemented by the PF4J catalog.
 */
public interface PluginSource {

    record LoadedPlugin(String pluginId, PluginManifest manifest, String source) {
    }

    List<LoadedPlugin> plugins();

    PluginManifest manifest(String pluginId);

    /**
     * Fresh entry instance. Implementations may swap the backing classloader when the artifact changed.
     */
    FoundationPlugin newInstance(String pluginId);
}
