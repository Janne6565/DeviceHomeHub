package com.janne.webserverraspberrypi.websockets;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@EnableScheduling
public class ServiceManagerWebsocketService implements WebSocketHandler {

    private Map<String, List<WebSocketSession>> sessions = new HashMap<>();

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    private void cleanUp() {
        System.out.println("Running cleanup");
        for (String key : sessions.keySet()) {
            List<WebSocketSession> lookingAt = sessions.get(key);
            lookingAt.removeIf(Objects::isNull);
            lookingAt.removeIf(session -> !session.isOpen());
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        return;
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String received = (String) message.getPayload();
        if (!sessions.containsKey(received)) {
            sessions.put(received, new ArrayList<>());
        }

        if (sessions.get(received).contains(session)) {
            return;
        }

        sessions.get(received).add(session);

        System.out.println("Registered new Listener for device_id: " + session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        for (String received : sessions.keySet()) {
            sessions.get(received).removeIf(webSocketSession -> webSocketSession.getId().equals(session.getId()));
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public void executeAction(String deviceId, String service, String action) {
        List<WebSocketSession> devices = sessions.getOrDefault(deviceId, new ArrayList<>());
        String compiled_message = "{'service': '" + encodeString(service) + "', 'action': '" + encodeString(action) + "'}";

        for (WebSocketSession session : new ArrayList<>(devices)) {
            try {
                session.sendMessage(new TextMessage(compiled_message));
            } catch (IOException | IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    private String encodeString(String str) {
        return str.replaceAll("\"", "\\\\\"");
    }
}
