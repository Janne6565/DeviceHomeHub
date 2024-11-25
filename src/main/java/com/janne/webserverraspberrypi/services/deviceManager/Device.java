package com.janne.webserverraspberrypi.services.deviceManager;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

@Data
@Builder
@ToString
public class Device {
    private String id;
    private String[] definedOperations;
    private String password;
    private String ip;
}
