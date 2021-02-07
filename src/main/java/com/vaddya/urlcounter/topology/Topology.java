package com.vaddya.urlcounter.topology;

import java.util.Set;

import org.jetbrains.annotations.NotNull;

public interface Topology {

    @NotNull
    static Topology consistentHashing(
            @NotNull final Set<String> topology,
            @NotNull final String me) {
        return new ConsistentHashingTopology(topology, me);
    }

    @NotNull
    Set<String> all();

    @NotNull
    String primaryFor(@NotNull String key);

    @NotNull
    String me();

    void addNode(@NotNull String node);

    void removeNode(@NotNull String node);
}
