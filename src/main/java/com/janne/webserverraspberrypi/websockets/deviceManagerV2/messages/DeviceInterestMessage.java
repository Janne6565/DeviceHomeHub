package com.janne.webserverraspberrypi.websockets.deviceManagerV2.messages;

import lombok.Data;

@Data
public class DeviceInterestMessage extends GenericWebSocketMessage {
    private String deviceId;
}
