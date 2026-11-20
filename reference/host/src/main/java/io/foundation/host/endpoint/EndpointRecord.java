package io.foundation.host.endpoint;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Instant;

/**
 * Managed endpoint (edge/desktop/mobile/relay). v1 only exposes the edge profile.
 */
public final class EndpointRecord {

    private final String id;
    private final String tenantId;
    private final String endpointToken;
    private final Instant createdAt = Instant.now();

    private volatile String name;
    private volatile String profile;
    private volatile ObjectNode platform;
    private volatile String version;
    private volatile ArrayNode capabilities;
    private volatile ObjectNode labels;
    private volatile String status = "offline";
    private volatile Instant lastSeen;
    private volatile String lastError;

    private volatile ObjectNode desired;
    private volatile ObjectNode reported;

    public EndpointRecord(String id, String tenantId, String endpointToken) {
        this.id = id;
        this.tenantId = tenantId;
        this.endpointToken = endpointToken;
    }

    public String id() {
        return id;
    }

    public String tenantId() {
        return tenantId;
    }

    public String endpointToken() {
        return endpointToken;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public String name() {
        return name;
    }

    public void name(String name) {
        this.name = name;
    }

    public String profile() {
        return profile;
    }

    public void profile(String profile) {
        this.profile = profile;
    }

    public ObjectNode platform() {
        return platform;
    }

    public void platform(ObjectNode platform) {
        this.platform = platform;
    }

    public String version() {
        return version;
    }

    public void version(String version) {
        this.version = version;
    }

    public ArrayNode capabilities() {
        return capabilities;
    }

    public void capabilities(ArrayNode capabilities) {
        this.capabilities = capabilities;
    }

    public ObjectNode labels() {
        return labels;
    }

    public void labels(ObjectNode labels) {
        this.labels = labels;
    }

    public String status() {
        return status;
    }

    public void status(String status) {
        this.status = status;
    }

    public Instant lastSeen() {
        return lastSeen;
    }

    public void lastSeen(Instant lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String lastError() {
        return lastError;
    }

    public void lastError(String lastError) {
        this.lastError = lastError;
    }

    public ObjectNode desired() {
        return desired;
    }

    public void desired(ObjectNode desired) {
        this.desired = desired;
    }

    public ObjectNode reported() {
        return reported;
    }

    public void reported(ObjectNode reported) {
        this.reported = reported;
    }
}
