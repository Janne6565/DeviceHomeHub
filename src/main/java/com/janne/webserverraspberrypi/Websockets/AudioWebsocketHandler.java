package com.janne.webserverraspberrypi.websockets;

import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AudioWebsocketHandler implements WebSocketHandler {
    private final Logger logger;
    private List<WebSocketSession> connections = new ArrayList<WebSocketSession>();
    private Map<String, List<String>> lastObjects = new HashMap<>();

    public AudioWebsocketHandler(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        connections.add(session);
        for (String key : lastObjects.keySet()) {
            List<String> lastObject = lastObjects.get(key);
            session.sendMessage(new TextMessage("{\"type\": \"" + key + "\", \"value\": \"" + lastObject.get(0) + "\", \"source\": \"" + lastObject.get(1) + "\"}"));
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

    public void sendInformation(String key, String value, String source) {
        List<String> object = new ArrayList<>();
        object.add(value);
        object.add(source);
        lastObjects.put(key, object);
        for (WebSocketSession session : new ArrayList<>(connections)) {
            try {
                WebSocketMessage<String> message = new TextMessage(
                        "{\"type\": \"" + key + "\", \"value\": \"" + value + "\", \"source\": \"" + source + "\"}"
                );
                session.sendMessage(message);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.out.println("Removing session: " + session);
                try {
                    session.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                connections.remove(session);
            }
        }
    }
}
