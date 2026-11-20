package io.foundation.api;

import java.util.List;

/**
 * Diagnostic view of one registered effect. Children are nested effects created by the same factory.
 */
public record EffectNode(String label, List<EffectNode> children) {

    public EffectNode {
        children = List.copyOf(children);
    }
}
