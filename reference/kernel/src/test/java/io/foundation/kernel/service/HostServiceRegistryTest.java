package io.foundation.kernel.service;

import io.foundation.api.ErrorCode;
import io.foundation.api.FoundationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HostServiceRegistryTest {

    @Test
    void serviceBecomesVisibleOnlyAfterOwnerActivates() {
        HostServiceRegistry registry = new HostServiceRegistry();
        ModuleServiceRegistry module = new ModuleServiceRegistry(registry, "mod_a");
        module.provide("demo.service", String.class, "value");

        assertThat(registry.available("demo.service")).isFalse();
        assertThat(registry.get("demo.service", String.class, false)).isNull();

        registry.activate("mod_a");
        assertThat(module.get("demo.service", String.class)).isEqualTo("value");

        registry.deactivate("mod_a");
        assertThat(registry.available("demo.service")).isFalse();
        assertThatThrownBy(() -> module.get("demo.service", String.class))
                .isInstanceOf(FoundationException.class)
                .satisfies(failure -> assertThat(((FoundationException) failure).getCode())
                        .isEqualTo(ErrorCode.DEPENDENCY_MISSING));
    }

    @Test
    void duplicateServiceIsRejected() {
        HostServiceRegistry registry = new HostServiceRegistry();
        new ModuleServiceRegistry(registry, "mod_a").provide("demo.service", String.class, "a");
        assertThatThrownBy(() -> new ModuleServiceRegistry(registry, "mod_b")
                .provide("demo.service", String.class, "b"))
                .isInstanceOf(FoundationException.class)
                .satisfies(failure -> assertThat(((FoundationException) failure).getCode())
                        .isEqualTo(ErrorCode.SERVICE_CONFLICT));
    }

    @Test
    void onlyOwnerCanSet() {
        HostServiceRegistry registry = new HostServiceRegistry();
        new ModuleServiceRegistry(registry, "mod_a").provide("demo.service", String.class, "a");
        registry.activate("mod_a");
        assertThatThrownBy(() -> new ModuleServiceRegistry(registry, "mod_b").set("demo.service", "b"))
                .isInstanceOf(FoundationException.class)
                .satisfies(failure -> assertThat(((FoundationException) failure).getCode())
                        .isEqualTo(ErrorCode.SERVICE_FORBIDDEN));
        new ModuleServiceRegistry(registry, "mod_a").set("demo.service", "updated");
        assertThat(registry.get("demo.service", String.class, true)).isEqualTo("updated");
    }

    @Test
    void unavailableListenerFiresOnce() {
        HostServiceRegistry registry = new HostServiceRegistry();
        java.util.List<String> events = new java.util.ArrayList<>();
        registry.onUnavailable(events::add);
        new ModuleServiceRegistry(registry, "mod_a").provide("demo.service", String.class, "a");
        registry.activate("mod_a");
        registry.deactivate("mod_a");
        assertThat(events).containsExactly("demo.service");
    }
}
