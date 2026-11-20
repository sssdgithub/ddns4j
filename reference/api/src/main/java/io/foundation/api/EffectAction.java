package io.foundation.api;

/**
 * An action that performs a side effect and returns its undo handle.
 */
@FunctionalInterface
public interface EffectAction {

    Disposable run() throws Exception;
}
