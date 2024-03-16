package com.janne.webserverraspberrypi.Websockets;

import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceManagerWebsocket implements WebSocketHandler {

    private static Map<String, List<String[]>> device_services = new HashMap<>();
    private static Map<String, List<WebSocketSession>> sessions = new HashMap<>();

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
        return;
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public static void executeAction(String deviceId, String service, String action) {
        List<WebSocketSession> devices = sessions.getOrDefault(deviceId, new ArrayList<>());
        String compiled_message = "{'service': '" + service + "', 'action': '" + action + "'}";

        for (WebSocketSession session : new ArrayList<>(devices)) {
            try {
                session.sendMessage(new TextMessage(compiled_message));
            } catch (IOException | IllegalStateException e) {
                devices.remove(session);
            }
        }

    }

}
