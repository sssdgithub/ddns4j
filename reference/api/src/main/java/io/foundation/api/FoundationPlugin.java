package io.foundation.api;

/**
 * Plugin entry contract. Implementations must be stateless between installs: the kernel may create
 * a new instance, or re-install the same instance after disposing all effects.
 */
public interface FoundationPlugin {

    String id();

    /**
     * Performs the installation and returns the undo handle for any additional resource that is not
     * registered through {@link PluginContext#effect}. Must be idempotent and re-entrant.
     */
    Disposable install(PluginContext context) throws Exception;
}
