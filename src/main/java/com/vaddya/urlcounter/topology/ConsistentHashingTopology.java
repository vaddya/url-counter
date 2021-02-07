package com.vaddya.urlcounter.topology;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.StampedLock;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public final class ConsistentHashingTopology implements Topology {
    private static final int VNODE_COUNT = 100;

    private final String me;
    private final Set<String> nodes;
    private final NavigableMap<Long, VirtualNode<String>> ring = new TreeMap<>();
    private final StampedLock lock = new StampedLock();
    private final HashFunction hashFunction = Hashing.murmur3_128(42);

    public ConsistentHashingTopology(
            @NotNull final Set<String> nodes,
            @NotNull final String me) {
        if (nodes.isEmpty()) {
            throw new IllegalArgumentException("Topology should not be empty");
        }
        this.me = me;
        this.nodes = new HashSet<>(nodes);
        nodes.forEach(this::addNode);
    }

    @Override
    @NotNull
    public Set<String> all() {
        final long stamp = lock.readLock();
        try {
            return nodes;
        } finally {
            lock.unlockRead(stamp);
        }
    }

    @Override
    @NotNull
    public String primaryFor(@NotNull final String key) {
        final long stamp = lock.readLock();
        try {
            final long hash = hash(key);
            final Map.Entry<Long, VirtualNode<String>> nodeEntry = ring.ceilingEntry(hash);
            if (nodeEntry == null) {
                return ring.firstEntry().getValue().node();
            }
            return nodeEntry.getValue().node();
        } finally {
            lock.unlockRead(stamp);
        }
    }

    @Override
    @NotNull
    public String me() {
        return me;
    }

    @Override
    public void addNode(@NotNull final String node) {
        final long stamp = lock.writeLock();
        try {
            nodes.add(node);
            for (int i = 0; i < VNODE_COUNT; i++) {
                final VirtualNode<String> vnode = new VirtualNode<>(node, i);
                final byte[] vnodeBytes = vnode.name().getBytes(Charsets.UTF_8);
                final long hash = hashFunction.hashBytes(vnodeBytes).asLong();
                ring.put(hash, vnode);
            }
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Override
    public void removeNode(@NotNull final String node) {
        final long stamp = lock.writeLock();
        try {
            nodes.remove(node);
            ring.entrySet().removeIf(e -> e.getValue().node().equals(node));
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    private long hash(@NotNull final String key) {
        return hashFunction.hashString(key, Charset.defaultCharset()).asLong();
    }

    private static class VirtualNode<T> {
        private final String node;
        private final int index;

        VirtualNode(
                @NotNull final String node,
                final int index) {
            this.node = node;
            this.index = index;
        }

        @NotNull
        String name() {
            return node + "_" + index;
        }

        @NotNull
        String node() {
            return node;
        }
    }
}
