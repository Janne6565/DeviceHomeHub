package com.janne.webserverraspberrypi.services.ambilight;

import com.janne.webserverraspberrypi.websockets.AudioWebsocketService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Music mode for the projector ambilight: while it is on, the LEDs show a gradient of the
 * current album cover's dominant colors instead of the HDMI grabber. Home Assistant decides
 * when it is on (amp not on a video input) and calls the controller.
 * <p>
 * The image goes out with a limited duration and is refreshed well before it expires, so if
 * the hub dies the strip falls back to the grabber by itself instead of staying stuck on
 * album colors during a movie.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AmbilightMusicService {

    static final String COVER_KEY = "albumCover";
    static final int PRIORITY = 100;
    static final String ORIGIN = "DeviceHomeHub music mode";
    static final Duration IMAGE_DURATION = Duration.ofMinutes(10);
    static final Duration REFRESH_INTERVAL = Duration.ofMinutes(4);

    private final AudioWebsocketService audioWebsocketService;
    private final HyperHdrClient hyperHdr;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private final AtomicBoolean enabled = new AtomicBoolean(false);
    /** All state below is only touched on this single thread. */
    private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "ambilight-music");
        thread.setDaemon(true);
        return thread;
    });

    private String frameCoverUrl;
    private BufferedImage frame;

    @PostConstruct
    void start() {
        if (!hyperHdr.isConfigured()) {
            log.warn("hyperhdr.url is not set, ambilight music mode is disabled");
            return;
        }
        audioWebsocketService.addListener((key, value) -> {
            if (COVER_KEY.equals(key) && enabled.get()) {
                worker.execute(() -> showCover(value));
            }
        });
        worker.scheduleWithFixedDelay(this::refresh, REFRESH_INTERVAL.toSeconds(), REFRESH_INTERVAL.toSeconds(), TimeUnit.SECONDS);
    }

    @PreDestroy
    void stop() {
        worker.shutdownNow();
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public boolean setEnabled(boolean value) {
        if (!hyperHdr.isConfigured()) {
            return false;
        }
        boolean changed = enabled.getAndSet(value) != value;
        if (value) {
            // Always re-render on "on": Home Assistant re-sends it after its own restarts, and the
            // HyperHDR side may have expired or been cleared by hand in the meantime.
            worker.execute(() -> showCover(audioWebsocketService.getLastValue(COVER_KEY)));
        } else {
            worker.execute(this::clear);
        }
        if (changed) {
            log.info("Ambilight music mode {}", value ? "on" : "off");
        }
        return value;
    }

    private void showCover(String coverUrl) {
        if (!enabled.get()) {
            return;
        }
        if (isUsableUrl(coverUrl) && !coverUrl.equals(frameCoverUrl)) {
            try {
                BufferedImage cover = download(coverUrl);
                List<AlbumPalette.Swatch> palette = AlbumPalette.extract(cover);
                frame = AlbumPalette.render(palette);
                frameCoverUrl = coverUrl;
                log.info("New ambilight palette for {}: {}", coverUrl, describe(palette));
            } catch (Exception e) {
                // Keep whatever palette we had: a flaky cover CDN should not blank the room.
                log.warn("Could not build palette from {}: {}", coverUrl, e.toString());
            }
        }
        push();
    }

    private void refresh() {
        if (enabled.get()) {
            push();
        }
    }

    private void push() {
        if (frame == null) {
            // Nothing played since the hub started: leave the grabber in charge.
            return;
        }
        try {
            hyperHdr.showImage(frame, PRIORITY, ORIGIN, IMAGE_DURATION);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Could not send ambilight palette to HyperHDR: {}", e.toString());
        }
    }

    private void clear() {
        if (enabled.get()) {
            return;
        }
        try {
            hyperHdr.clear(PRIORITY);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Could not clear ambilight music priority: {}", e.toString());
        }
    }

    private BufferedImage download(String coverUrl) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(coverUrl)).timeout(Duration.ofSeconds(10)).GET().build();
        HttpResponse<byte[]> response = http.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode());
        }
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(response.body()));
        if (image == null) {
            throw new IOException("not a readable image");
        }
        return image;
    }

    private static boolean isUsableUrl(String value) {
        return value != null && (value.startsWith("http://") || value.startsWith("https://"));
    }

    private static String describe(List<AlbumPalette.Swatch> palette) {
        return palette.stream()
                .map(s -> String.format("#%06x %.0f%%", s.color().getRGB() & 0xFFFFFF, s.weight() * 100))
                .toList()
                .toString();
    }
}
