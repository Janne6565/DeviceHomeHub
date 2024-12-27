package com.janne.webserverraspberrypi.websockets.deviceManagerV2.messages;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeviceActionMessage {
    private String deviceId;
    private String deviceSecret;
    private String action;
}
