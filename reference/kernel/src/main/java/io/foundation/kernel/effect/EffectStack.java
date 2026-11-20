package io.foundation.kernel.effect;

import io.foundation.api.Disposable;
import io.foundation.api.EffectAction;
import io.foundation.api.EffectNode;
import io.foundation.api.ErrorCode;
import io.foundation.api.FoundationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * LIFO effect stack owned by one module. Registering after disposal fails with
 * {@code INACTIVE_EFFECT}; disposing twice is a no-op.
 */
public final class EffectStack {

    private static final Logger log = LoggerFactory.getLogger(EffectStack.class);

    private final String ownerId;
    private final Node root;
    private final Deque<Node> open = new ArrayDeque<>();
    private boolean disposed;

    public EffectStack(String ownerId) {
        this.ownerId = ownerId;
        this.root = new Node("module:" + ownerId);
    }

    public synchronized Disposable register(String label, EffectAction action) {
        if (disposed) {
            throw new FoundationException(ErrorCode.INACTIVE_EFFECT,
                    "module " + ownerId + " is unloaded, cannot register effect " + label);
        }
        Node node = new Node(label);
        Node parent = open.isEmpty() ? root : open.peek();
        parent.children.add(node);
        open.push(node);
        try {
            Disposable disposer = action.run();
            node.disposer = disposer == null ? Disposable.noop() : disposer;
            return () -> disposeNode(node);
        } catch (Throwable failure) {
            disposeNode(node);
            if (failure instanceof RuntimeException runtime) {
                throw runtime;
            }
            if (failure instanceof Error error) {
                throw error;
            }
            throw new RuntimeException("effect " + label + " failed", failure);
        } finally {
            open.pop();
        }
    }

    public void dispose() {
        synchronized (this) {
            if (disposed) {
                return;
            }
            disposed = true;
        }
        for (Node child : reversed(root.children)) {
            disposeNode(child);
        }
    }

    public synchronized boolean isDisposed() {
        return disposed;
    }

    public synchronized boolean isEmpty() {
        return root.children.stream().allMatch(Node::isDisposed);
    }

    public synchronized EffectNode snapshot() {
        List<EffectNode> children = new ArrayList<>();
        for (Node node : root.children) {
            if (!node.disposed) {
                children.add(node.snapshot());
            }
        }
        return new EffectNode("module:" + ownerId, children);
    }

    private void disposeNode(Node node) {
        synchronized (this) {
            if (node.disposed) {
                return;
            }
            node.disposed = true;
        }
        for (Node child : reversed(node.children)) {
            disposeNode(child);
        }
        Disposable disposer = node.disposer;
        if (disposer == null) {
            return;
        }
        try {
            disposer.dispose();
        } catch (Throwable failure) {
            log.warn("effect {} of module {} failed to roll back: {}", node.label, ownerId,
                    failure.toString());
        }
    }

    private static List<Node> reversed(List<Node> nodes) {
        List<Node> copy = new ArrayList<>(nodes);
        java.util.Collections.reverse(copy);
        return copy;
    }

    private static final class Node {
        private final String label;
        private final List<Node> children = new ArrayList<>();
        private Disposable disposer;
        private boolean disposed;

        private Node(String label) {
            this.label = label;
        }

        private boolean isDisposed() {
            return disposed;
        }

        private EffectNode snapshot() {
            List<EffectNode> kids = children.stream()
                    .filter(child -> !child.disposed)
                    .map(Node::snapshot)
                    .toList();
            return new EffectNode(label, kids);
        }
    }
}
