package com.vaddya.urlcounter.topology;

import java.util.Set;

import org.jetbrains.annotations.NotNull;

public interface Topology {

    @NotNull
    Set<String> all();

    @NotNull
    String primaryFor(@NotNull String key);

    @NotNull
    String me();

    void addNode(@NotNull String node);

    void removeNode(@NotNull String node);
}
