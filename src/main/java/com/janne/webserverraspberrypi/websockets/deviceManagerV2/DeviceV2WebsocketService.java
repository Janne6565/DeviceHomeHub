package com.janne.webserverraspberrypi.websockets.deviceManagerV2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.janne.webserverraspberrypi.websockets.deviceManagerV2.messages.DeviceActionMessage;
import com.janne.webserverraspberrypi.websockets.deviceManagerV2.messages.DeviceInterestMessage;
import com.janne.webserverraspberrypi.websockets.deviceManagerV2.messages.DeviceStatusInfoMessage;
import com.janne.webserverraspberrypi.websockets.deviceManagerV2.messages.GenericWebSocketMessage;
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
public class DeviceV2WebsocketService implements WebSocketHandler {

    private final ObjectMapper jacksonObjectMapper;
    private Map<String, List<WebSocketSession>> sessions = new HashMap<>();
    private Map<String, String> lastAction = new HashMap<>();
    private Map<String, String> lastSecret = new HashMap<>();
    private Map<String, String> deviceStatus = new HashMap<>();

    public DeviceV2WebsocketService(ObjectMapper jacksonObjectMapper) {
        this.jacksonObjectMapper = jacksonObjectMapper;
    }

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    private void cleanUp() {
        log.info("Cleaning up");
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
        String messageValue = (String) message.getPayload();
        GenericWebSocketMessage webSocketMessage = jacksonObjectMapper.readValue(messageValue, GenericWebSocketMessage.class);
        switch (webSocketMessage.getType()) {
            case "INTEREST": {
                DeviceInterestMessage interestMessage = jacksonObjectMapper.readValue(messageValue, DeviceInterestMessage.class);
                registerSession(session, interestMessage.getDeviceId());
                if (lastAction.containsKey(interestMessage.getDeviceId())) {
                    DeviceActionMessage actionMessage = DeviceActionMessage.builder()
                            .deviceSecret(lastSecret.get(interestMessage.getDeviceId()))
                            .action(lastAction.get(interestMessage.getDeviceId()))
                            .deviceId(interestMessage.getDeviceId())
                                    .build();

                    sendMessage(session, actionMessage);
                }
                break;
            }
            case "INFO": {
                DeviceStatusInfoMessage statusInfo = jacksonObjectMapper.readValue(messageValue, DeviceStatusInfoMessage.class);
                deviceStatus.put(statusInfo.getDeviceId(), statusInfo.getStatus());
                break;
            }
        }

    }

    private void registerSession(WebSocketSession session, String deviceId) {
        if (!sessions.containsKey(deviceId)) {
            sessions.put(deviceId, new ArrayList<>());
        }

        if (sessions.get(deviceId).contains(session)) {
            return;
        }

        sessions.get(deviceId).add(session);
        log.info("Registered new websocket for device {}", deviceId);
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

    public void executeAction(String deviceId, String deviceSecret, String action) {
        lastAction.put(deviceId, action);
        lastSecret.put(deviceId, deviceSecret);

        List<WebSocketSession> devices = sessions.getOrDefault(deviceId, new ArrayList<>());
        DeviceActionMessage actionMessage = DeviceActionMessage.builder()
                .deviceId(deviceId)
                .deviceSecret(deviceSecret)
                .action(action).build();

        for (WebSocketSession session : new ArrayList<>(devices)) {
            sendMessage(session, actionMessage);
        }
    }

    private void sendMessage(WebSocketSession session, DeviceActionMessage actionMessage) {
        String compiledMessage = null;
        try {
            compiledMessage = jacksonObjectMapper.writeValueAsString(actionMessage);
            session.sendMessage(new TextMessage(compiledMessage));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String encodeString(String str) {
        return str.replaceAll("\"", "\\\\\"");
    }

    public String getDeviceStatus(String deviceId) {
        return deviceStatus.getOrDefault(deviceId, "0");
    }
}
