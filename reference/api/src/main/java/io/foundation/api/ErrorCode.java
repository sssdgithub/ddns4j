package io.foundation.api;

/**
 * Error codes defined by the plugin contract (docs/02-plugin-contract.md §11).
 */
public enum ErrorCode {
    INACTIVE_EFFECT,
    SERVICE_CONFLICT,
    SERVICE_FORBIDDEN,
    DEPENDENCY_MISSING,
    CONFIG_INVALID,
    TWIN_SCHEMA_INVALID,
    API_VERSION_MISMATCH,
    MANIFEST_INVALID,
    MODULE_INACTIVE
}
