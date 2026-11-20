package io.foundation.api;

/**
 * Reversible side effect. Disposing must be idempotent and is allowed to be asynchronous.
 */
@FunctionalInterface
public interface Disposable extends AutoCloseable {

    void dispose();

    @Override
    default void close() {
        dispose();
    }

    static Disposable noop() {
        return () -> {
        };
    }

    default Disposable andThen(Disposable next) {
        return () -> {
            dispose();
            next.dispose();
        };
    }
}
