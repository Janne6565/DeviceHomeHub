package com.janne.webserverraspberrypi.websockets.deviceManagerV2.messages;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DeviceStatusInfoMessage extends GenericWebSocketMessage {
    private String deviceId;
    private String status;
}
