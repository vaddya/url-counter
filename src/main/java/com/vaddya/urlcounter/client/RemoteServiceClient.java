package com.vaddya.urlcounter.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.vaddya.urlcounter.Paths;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RemoteServiceClient implements ServiceClient {
    private static final Logger log = LoggerFactory.getLogger(RemoteServiceClient.class);
    private static final int TIMEOUT_MILLIS = 1000;

    private final String baseUrl;
    private final HttpClient client;
    private final ObjectMapper json;

    public RemoteServiceClient(@NotNull final String baseUrl) {
        this.baseUrl = baseUrl;
        this.client = HttpClient.newHttpClient();
        this.json = new ObjectMapper();
    }

    @Override
    @NotNull
    public CompletableFuture<Void> addAsync(@NotNull final String domain) {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + Paths.ADD + "/" + domain))
                .timeout(Duration.ofMillis(TIMEOUT_MILLIS))
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .thenApply(x -> null);
    }

    @Override
    @NotNull
    public CompletableFuture<List<String>> topAsync(int n) {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + Paths.TOP + "/" + n))
                .timeout(Duration.ofMillis(TIMEOUT_MILLIS))
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(this::parseList);
    }

    @Override
    @NotNull
    public CompletableFuture<Map<String, Integer>> topCountAsync(final int n) {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + Paths.COUNTS + "/" + n))
                .timeout(Duration.ofMillis(TIMEOUT_MILLIS))
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(this::parseKeyValue);
    }

    @NotNull
    private List<String> parseList(@NotNull final String value) {
        try {
            return json.readValue(value, TypeFactory.collectionType(List.class, String.class));
        } catch (IOException e) {
            log.error("Cannot parse string list: " + value, e);
            return Collections.emptyList();
        }
    }

    @NotNull
    private Map<String, Integer> parseKeyValue(@NotNull final String value) {
        try {
            return json.readValue(value, TypeFactory.mapType(Map.class, String.class, Integer.class));
        } catch (IOException e) {
            log.error("Cannot parse string list: " + value, e);
            return Collections.emptyMap();
        }
    }
}
