package com.vaddya.urlcounter.client;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.vaddya.urlcounter.local.UrlCounter;
import org.jetbrains.annotations.NotNull;

public class LocalServiceClient implements ServiceClient {
    private final UrlCounter counter;

    public LocalServiceClient(@NotNull final UrlCounter counter) {
        this.counter = counter;
    }

    @Override
    @NotNull
    public CompletableFuture<Void> addAsync(@NotNull final String domain) {
        return CompletableFuture.runAsync(() -> counter.add(domain));
    }

    @Override
    public @NotNull CompletableFuture<List<String>> topAsync(int n) {
        return CompletableFuture.supplyAsync(() -> counter.top(n));
    }

    @Override
    @NotNull
    public CompletableFuture<Map<String, Integer>> topCountAsync(final int n) {
        return CompletableFuture.supplyAsync(() -> counter.topCount(n));
    }
}
