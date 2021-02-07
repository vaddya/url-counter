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

import com.vaddya.urlcounter.Endpoints;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RemoteServiceClient implements ServiceClient {
    private static final Logger log = LoggerFactory.getLogger(RemoteServiceClient.class);
    private static final Duration TIMEOUT = Duration.ofMillis(1000);

    private final String baseUrl;
    private final ObjectMapper json;
    private final HttpClient client = HttpClient.newHttpClient();

    public RemoteServiceClient(
            @NotNull final String baseUrl,
            @NotNull final ObjectMapper json) {
        this.baseUrl = baseUrl;
        this.json = json;
    }

    @Override
    @NotNull
    public CompletableFuture<Void> addAsync(@NotNull final String domain) {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + Endpoints.ADD + "/" + domain))
                .timeout(TIMEOUT)
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .thenApply(x -> null);
    }

    @Override
    @NotNull
    public CompletableFuture<List<String>> topAsync(int n) {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + Endpoints.TOP + "/" + n))
                .timeout(TIMEOUT)
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(this::parseList);
    }

    @Override
    @NotNull
    public CompletableFuture<Map<String, Integer>> topCountAsync(final int n) {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/" + Endpoints.COUNTS + "/" + n))
                .timeout(TIMEOUT)
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
            log.error("Cannot parse key-values: " + value, e);
            return Collections.emptyMap();
        }
    }
}
