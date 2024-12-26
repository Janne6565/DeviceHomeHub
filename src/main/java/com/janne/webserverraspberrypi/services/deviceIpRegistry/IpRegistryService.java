package com.janne.webserverraspberrypi.services.deviceIpRegistry;

import ch.qos.logback.core.joran.sanity.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class IpRegistryService {

    private final Map<String, String> ipRegistry = new HashMap<>();
    private final Map<String, String> deviceSecrets = new HashMap<>();
    @Value("${device_infos}")
    private List<String> deviceInfos;
    private boolean initiated = false;

    private void init() {
        for (String device : deviceInfos) {
            String[] infos = device.split(":");
            String deviceId = infos[0].strip();
            String deviceSecret = infos[1];
            log.info("Registering device {} {} {}", deviceId, deviceSecret, device);
            deviceSecrets.put(deviceId, deviceSecret);
        }
        initiated = true;
    }

    public String getDeviceIp(String deviceId) {
        return ipRegistry.get(deviceId);
    }

    public void setDeviceIp(String deviceId, String ip, String secret) {
        if (!initiated) {
            init();
        }
        if (!deviceSecrets.containsKey(deviceId)) {
            throw new IllegalArgumentException("Device (" + deviceId + ") is not registered");
        }
        if (!deviceSecrets.get(deviceId).equals(secret)) {
            throw new IllegalArgumentException("Device id " + deviceId + " does not match secret");
        }
        ipRegistry.put(deviceId, ip);
    }

    public Set<Map.Entry<String, String>> getEntries() {
        return ipRegistry.entrySet();
    }
}
