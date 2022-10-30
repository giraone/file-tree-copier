package com.giraone.io.copier.web.http;

import com.giraone.io.copier.ReadFromUrlStreamProvider;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class DefaultHttpClientInputStreamProvider implements ReadFromUrlStreamProvider {

    @Override
    public InputStream openInputStream(URL url) {

        try {
            final URI uri = url.toURI();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.of(10, ChronoUnit.SECONDS))
                .GET()
                .build();
            HttpResponse.BodyHandler<InputStream> bodyHandler = HttpResponse.BodyHandlers.ofInputStream();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<InputStream> response = client.send(request, bodyHandler);
            return response.body();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // TODO: Add hooks
}
