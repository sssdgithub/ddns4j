package io.foundation.kernel.effect;

import io.foundation.api.Disposable;
import io.foundation.api.EffectNode;
import io.foundation.api.ErrorCode;
import io.foundation.api.FoundationException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EffectStackTest {

    @Test
    void disposesEffectsInLifoOrder() {
        EffectStack stack = new EffectStack("mod_test");
        List<String> order = new ArrayList<>();
        stack.register("first", () -> () -> order.add("first"));
        stack.register("second", () -> () -> order.add("second"));
        stack.register("third", () -> () -> order.add("third"));

        stack.dispose();

        assertThat(order).containsExactly("third", "second", "first");
        assertThat(stack.isEmpty()).isTrue();
        assertThat(stack.snapshot().children()).isEmpty();
    }

    @Test
    void nestedEffectsDisposeChildrenBeforeParent() {
        EffectStack stack = new EffectStack("mod_test");
        List<String> order = new ArrayList<>();
        stack.register("parent", () -> {
            stack.register("child-a", () -> () -> order.add("child-a"));
            stack.register("child-b", () -> () -> order.add("child-b"));
            return () -> order.add("parent");
        });

        EffectNode tree = stack.snapshot();
        assertThat(tree.children()).hasSize(1);
        assertThat(tree.children().get(0).children()).hasSize(2);

        stack.dispose();
        assertThat(order).containsExactly("child-b", "child-a", "parent");
    }

    @Test
    void disposeIsIdempotent() {
        EffectStack stack = new EffectStack("mod_test");
        List<String> order = new ArrayList<>();
        Disposable handle = stack.register("once", () -> () -> order.add("once"));
        handle.dispose();
        handle.dispose();
        stack.dispose();
        assertThat(order).containsExactly("once");
    }

    @Test
    void registeringAfterDisposeFailsWithInactiveEffect() {
        EffectStack stack = new EffectStack("mod_test");
        stack.dispose();
        assertThatThrownBy(() -> stack.register("late", () -> Disposable.noop()))
                .isInstanceOf(FoundationException.class)
                .satisfies(failure -> assertThat(((FoundationException) failure).getCode())
                        .isEqualTo(ErrorCode.INACTIVE_EFFECT));
    }

    @Test
    void failedRegistrationStillRollsBackChildren() {
        EffectStack stack = new EffectStack("mod_test");
        List<String> order = new ArrayList<>();
        assertThatThrownBy(() -> stack.register("broken", () -> {
            stack.register("child", () -> () -> order.add("child"));
            throw new IllegalStateException("boom");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(order).containsExactly("child");
        assertThat(stack.isEmpty()).isTrue();
    }
}
