package io.foundation.kernel.runtime;

/**
 * Module lifecycle states from docs/02-plugin-contract.md §3.
 */
public enum ModuleState {
    RESOLVED,
    ACTIVE,
    STOPPED,
    FAILED,
    UNINSTALLED
}
