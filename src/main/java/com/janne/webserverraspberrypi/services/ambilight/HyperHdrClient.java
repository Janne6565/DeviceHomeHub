package com.janne.webserverraspberrypi.services.ambilight;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Minimal HyperHDR JSON-RPC client: push an image at a priority, or clear that priority again so
 * the grabber (priority 240) takes over.
 */
@Slf4j
@Component
public class HyperHdrClient {

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${hyperhdr.url:}")
    private String url;

    public boolean isConfigured() {
        return url != null && !url.isBlank();
    }

    public void showImage(BufferedImage image, int priority, String origin, Duration duration) throws IOException, InterruptedException {
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(image, "png", png);

        Map<String, Object> command = new LinkedHashMap<>();
        command.put("command", "image");
        command.put("priority", priority);
        command.put("origin", origin);
        command.put("format", "auto");
        command.put("imagedata", Base64.getEncoder().encodeToString(png.toByteArray()));
        command.put("duration", duration.toMillis());
        send(command);
    }

    public void clear(int priority) throws IOException, InterruptedException {
        Map<String, Object> command = new LinkedHashMap<>();
        command.put("command", "clear");
        command.put("priority", priority);
        send(command);
    }

    private void send(Map<String, Object> command) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(command)))
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        Map<?, ?> body = objectMapper.readValue(response.body(), Map.class);
        if (response.statusCode() != 200 || !Boolean.TRUE.equals(body.get("success"))) {
            throw new IOException("HyperHDR rejected " + command.get("command") + ": " + response.statusCode() + " " + response.body());
        }
    }
}
