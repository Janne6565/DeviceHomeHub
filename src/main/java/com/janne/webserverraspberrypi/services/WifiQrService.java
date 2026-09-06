package com.janne.webserverraspberrypi.services;

import com.janne.webserverraspberrypi.websockets.AudioWebsocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Drives the wifi QR code overlay on the led matrix. The hub only holds the on/off flag,
 * the matrix renders the code itself so no credentials ever pass through here.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WifiQrService {

    public static final String MESSAGE_TYPE = "showQrCode";
    private static final String SOURCE = "led-matrix-wifi-qr";

    private final AudioWebsocketService audioWebsocketService;
    private final AtomicBoolean shown = new AtomicBoolean(false);

    public boolean isShown() {
        return shown.get();
    }

    public boolean setShown(boolean value) {
        shown.set(value);
        publish();
        return value;
    }

    public boolean toggle() {
        return setShown(!shown.get());
    }

    private void publish() {
        log.info("Setting wifi qr code visibility to {}", shown.get());
        audioWebsocketService.sendInformation(MESSAGE_TYPE, String.valueOf(shown.get()), SOURCE, System.currentTimeMillis());
    }
}
