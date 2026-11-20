package io.foundation.host.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "foundation")
public class FoundationProperties {

    /**
     * Directory scanned for plugin jars (PF4J). Default ./plugins.
     */
    private Path pluginsDir = Path.of("plugins");

    /**
     * Directory scanned for app bundles (kind=app manifests). Default ./apps.
     */
    private Path appsDir = Path.of("apps");

    /**
     * Console administrator credentials (single admin in v1).
     */
    private String adminUser = "admin";
    private String adminPassword = "admin";

    /**
     * Endpoint is offline when this many seconds passed since the last heartbeat.
     */
    private int endpointOfflineSeconds = 90;

    public Path getPluginsDir() {
        return pluginsDir;
    }

    public void setPluginsDir(Path pluginsDir) {
        this.pluginsDir = pluginsDir;
    }

    public Path getAppsDir() {
        return appsDir;
    }

    public void setAppsDir(Path appsDir) {
        this.appsDir = appsDir;
    }

    public String getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(String adminUser) {
        this.adminUser = adminUser;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public int getEndpointOfflineSeconds() {
        return endpointOfflineSeconds;
    }

    public void setEndpointOfflineSeconds(int endpointOfflineSeconds) {
        this.endpointOfflineSeconds = endpointOfflineSeconds;
    }
}
