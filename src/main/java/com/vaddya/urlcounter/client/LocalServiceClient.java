package com.vaddya.urlcounter.client;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.vaddya.urlcounter.local.HitCounter;
import org.jetbrains.annotations.NotNull;

public final class LocalServiceClient implements ServiceClient {
    private final HitCounter counter;
    private final Executor executor;

    public LocalServiceClient(@NotNull final HitCounter counter) {
        this.counter = counter;
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Override
    @NotNull
    public CompletableFuture<Void> addAsync(@NotNull final String domain) {
        return CompletableFuture.runAsync(() -> counter.add(domain), executor);
    }

    @Override
    @NotNull
    public CompletableFuture<List<String>> topAsync(int n) {
        return CompletableFuture.supplyAsync(() -> counter.top(n), executor);
    }

    @Override
    @NotNull
    public CompletableFuture<Map<String, Integer>> topCountAsync(final int n) {
        return CompletableFuture.supplyAsync(() -> counter.topCount(n), executor);
    }
}
