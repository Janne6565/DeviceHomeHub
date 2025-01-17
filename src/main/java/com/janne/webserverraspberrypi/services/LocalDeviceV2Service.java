package com.janne.webserverraspberrypi.services;

import com.janne.webserverraspberrypi.services.deviceIpRegistry.IpRegistryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocalDeviceV2Service {
    private final IpRegistryService ipRegistryService;
    private final RequestService requestService;

    public String startDevice(String deviceId, String deviceSecret) {
        return sendDeviceRequest("start", deviceId, deviceSecret);
    }

    public String stopDevice(String deviceId, String deviceSecret) {
        return sendDeviceRequest("stop", deviceId, deviceSecret);
    }

    public String toggleDevice(String deviceId, String deviceSecret) {
        return sendDeviceRequest("toggle", deviceId, deviceSecret);
    }

    public String getDeviceStatus(String deviceId, String deviceSecret) {
        return sendDeviceRequest("get", deviceId, deviceSecret);
    }

    private String sendDeviceRequest(String operation, String deviceId, String deviceSecret) {
        String deviceIp = ipRegistryService.getDeviceIp(deviceId);
        if (deviceIp != null) {
            return "Not Found";
        }
        return requestService.sendRequest("POST", deviceIp + "/" + deviceSecret + "/" + operation, "");
    }
}
