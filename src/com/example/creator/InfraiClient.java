package com.example.creator;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class InfraiClient {
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final String baseUrl;
    private final String apiKey;

    public InfraiClient(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    public String request(String method, String path, String body) throws IOException, InterruptedException {
        String target = baseUrl.endsWith("/v1") && path.startsWith("/v1/")
                ? baseUrl + path.substring(3) : baseUrl + path;
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(target))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        for (int attempt = 0; attempt < 3; attempt++) {
            HttpResponse<String> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            String envelope = response.body();
            if (envelope.contains("\"ok\":false")) {
                throw new IOException("Infrai request rejected: " + envelope);
            }
            if (response.statusCode() == 429) {
                String retry = response.headers().firstValue("Retry-After").orElse("1");
                Thread.sleep(Math.min(8000L, Long.parseLong(retry) * (1L << attempt) * 1000L));
                continue;
            }
            if (response.statusCode() >= 500) throw new IOException("Infrai transport failure: HTTP " + response.statusCode());
            return envelope;
        }
        throw new IOException("Infrai request was rate limited");
    }
}
