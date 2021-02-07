package com.vaddya.urlcounter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Utils {
    private Utils() {
    }

    @Nullable
    public static String extractDomain(@NotNull final String url) {
        try {
            final URI uri = new URI("http://" + url);
            String domain = uri.getHost();
            domain = domain.startsWith("www.") ? domain.substring(4) : domain;
            final String[] parts = domain.split("\\.");
            if (parts.length <= 2) {
                return domain;
            } else {
                return parts[parts.length - 2] + "." + parts[parts.length - 1];
            }
        } catch (Exception e) {
            return null;
        }
    }

    @NotNull
    public static <T> CompletableFuture<List<T>> join(@NotNull final List<CompletableFuture<T>> futures) {
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()));
    }
}
