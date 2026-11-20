package io.foundation.kernel.runtime;

public record ModuleTarget(String kind, String endpointId) {

    public static ModuleTarget host() {
        return new ModuleTarget("host", null);
    }

    public static ModuleTarget endpoint(String endpointId) {
        return new ModuleTarget("endpoint", endpointId);
    }

    public boolean isHost() {
        return "host".equals(kind);
    }
}
