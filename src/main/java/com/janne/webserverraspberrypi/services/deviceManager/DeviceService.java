package com.janne.webserverraspberrypi.services.deviceManager;

import com.janne.webserverraspberrypi.Util;
import com.janne.webserverraspberrypi.services.RequestService;
import com.janne.webserverraspberrypi.websockets.ServiceManagerWebsocket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class DeviceService {

    private final Util util;
    private final WebClient webClient;
    private final RequestService requestService;
    private Map<String, Device> parsedDevices = new HashMap<>();
    private boolean initialized = false;
    private Map<String, Map<String, Boolean>> servicesRunning = new HashMap<>();

    @Value("${devices}")
    private String deviceIds;
    @Value("${device_password}")
    private String devicePasswords;
    @Value("${device_operations}")
    private String deviceOperations;
    @Value("${device_ips}")
    private String deviceIps;

    public DeviceService(Util util, WebClient webClient, RequestService requestService) {
        this.util = util;
        this.webClient = webClient;
        this.requestService = requestService;
    }

    private void initialize() {
        String[] deviceIdsSeperated = deviceIds.split(",");
        String[] devicePasswordsSeperated = devicePasswords.split(",");
        String[] deviceOperationsSeperated = deviceOperations.split(",");
        String[] deviceIpsSeperated = deviceIps.split(",");

        for (int i = 0; i < deviceIdsSeperated.length; i++) {
            parsedDevices.put(deviceIdsSeperated[i],
                    Device.builder()
                            .id(deviceIdsSeperated[i])
                            .definedOperations(deviceOperationsSeperated[i].split("\\|"))
                            .password(devicePasswordsSeperated[i])
                            .ip(deviceIpsSeperated[i])
                            .build()
            );
            System.out.println("Created new device: " + parsedDevices.get(deviceIdsSeperated[i]));
        }
    }

    public String executeAction(String deviceId, String operation) {
        if (!initialized) {
            initialized = true;
            initialize();
        }

        if (!parsedDevices.containsKey(deviceId)) {
            return null;
        }

        Device device = parsedDevices.get(deviceId);

        String actionUrl = "http://" + device.getIp() + "/" + device.getPassword() + "/" + operation;
        return requestService.sendRequest("POST", actionUrl, "");
    }

    public void startService(String deviceId, String serviceName) {
        if (!servicesRunning.containsKey(deviceId)) {
            servicesRunning.put(deviceId, new HashMap<>());
        }

        servicesRunning.get(deviceId).put(serviceName, true);

        ServiceManagerWebsocket.executeAction(deviceId, serviceName, "start");
    }

    public void stopService(String deviceId, String serviceName) {
        if (!servicesRunning.containsKey(deviceId)) {
            servicesRunning.put(deviceId, new HashMap<>());
        }

        servicesRunning.get(deviceId).put(serviceName, false);
        ServiceManagerWebsocket.executeAction(deviceId, serviceName, "stop");
    }

    public void restartService(String deviceId, String serviceName) {
        if (!servicesRunning.containsKey(deviceId)) {
            servicesRunning.put(deviceId, new HashMap<>());
        }

        servicesRunning.get(deviceId).put(serviceName, true);
        ServiceManagerWebsocket.executeAction(deviceId, serviceName, "restart");
    }

    public boolean isServiceRunning(String deviceId, String serviceName) {
        if (!servicesRunning.containsKey(deviceId)) {
            return false;
        }

        return servicesRunning.get(deviceId).getOrDefault(serviceName, false);
    }
}
