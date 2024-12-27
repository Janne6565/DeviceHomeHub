package com.janne.webserverraspberrypi.websockets;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@EnableScheduling
public class AudioWebsocketService implements WebSocketHandler {
    private List<WebSocketSession> connections = new ArrayList<WebSocketSession>();
    private Map<String, List<String>> lastObjects = new HashMap<>();

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    private void cleanUp() {
        log.info("Running cleanup");
        connections.removeIf(Objects::isNull);
        connections.removeIf(session -> !session.isOpen());
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        connections.add(session);
        for (String key : lastObjects.keySet()) {
            List<String> lastObject = lastObjects.get(key);
            session.sendMessage(buildMessage(key, lastObject));
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        connections.removeIf(webSocketSession -> webSocketSession.getId().equals(session.getId()));
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public void sendInformation(String key, String value, String source, float time) {
        List<String> object = new ArrayList<>();
        object.add(value);
        object.add(source);
        object.add(String.valueOf(time));
        lastObjects.put(key, object);
        connections.removeIf(Objects::isNull);
        for (WebSocketSession session : new ArrayList<>(connections)) {
            if (session == null) {
                continue;
            }

            try {
                WebSocketMessage<String> message = buildMessage(key, object);
                session.sendMessage(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private TextMessage buildMessage(String type, List<String> item) {
        return new TextMessage("{\"type\": \"" + encodeString(type) + "\", \"value\": \"" + encodeString(item.get(0)) + "\", \"source\": \"" + encodeString(item.get(1)) + "\", \"updated\": " + encodeString(item.get(2)) + "}");
    }

    private String encodeString(String str) {
        return str.replaceAll("\"", "\\\\\"");
    }
}
