package com.vaddya.urlcounter.client;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NotNull;

public interface ServiceClient {
    @NotNull
    CompletableFuture<Void> addAsync(@NotNull String domain);

    @NotNull
    CompletableFuture<List<String>> topAsync(int n);

    @NotNull
    CompletableFuture<Map<String, Integer>> topCountAsync(int n);
}
