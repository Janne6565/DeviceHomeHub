package com.janne.webserverraspberrypi.Websockets;

import org.springframework.web.socket.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AudioWebsocketHandler implements WebSocketHandler {
    private static List<WebSocketSession> connections = new ArrayList<WebSocketSession>();
    private static Map<String, Object> lastObject = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        connections.add(session);
        for (String key : lastObject.keySet()) {
            session.sendMessage(new TextMessage("{\"type\": \"" + key + "\", \"value\": \"" + lastObject.get(key) + "\"}"));
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
        for (WebSocketSession s : new ArrayList<>(connections)) {
            if (s.getId().equals(session.getId())) {
                connections.remove(s);
                break;
            }
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public static void sendInformation(String key, String value) {
        lastObject.put(key, value);
        for (WebSocketSession session : new ArrayList<>(connections)) {
            try {
                session.sendMessage(new TextMessage("{\"type\": \"" + key + "\", \"value\": \"" + value + "\"}"));
            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.out.println("Removing session: " + session.getId());
                connections.remove(session);
            }
        }
    }
}
