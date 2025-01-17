package com.janne.webserverraspberrypi.controller;

import com.janne.webserverraspberrypi.websockets.deviceManagerV2.DeviceV2WebsocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/deviceV2")
public class DeviceV2Controller {

    private final DeviceV2WebsocketService deviceV2WebsocketService;

    @GetMapping("/status/{deviceId}")
    public ResponseEntity<String> getDeviceStatusGet(@PathVariable("deviceId") String deviceId) {
        return ResponseEntity.ok(deviceV2WebsocketService.getDeviceStatus(deviceId));
    }

    @PostMapping("/status/{deviceId}")
    public ResponseEntity<String> getDeviceStatus(@PathVariable("deviceId") String deviceId) {
        return ResponseEntity.ok(deviceV2WebsocketService.getDeviceStatus(deviceId));
    }

    @PostMapping("/start/{deviceId}/{deviceSecret}")
    public ResponseEntity<Void> startDevice(@PathVariable("deviceId") String deviceId, @PathVariable("deviceSecret") String deviceSecret) {
        deviceV2WebsocketService.executeAction(deviceId, deviceSecret, "start");
        return ResponseEntity.ok(null);
    }

    @PostMapping("/stop/{deviceId}/{deviceSecret}")
    public ResponseEntity<Void> stopDevice(@PathVariable("deviceId") String deviceId, @PathVariable("deviceSecret") String deviceSecret) {
        deviceV2WebsocketService.executeAction(deviceId, deviceSecret, "stop");
        return ResponseEntity.ok(null);
    }

    @PostMapping("/toggle/{deviceId}/{deviceSecret}")
    public ResponseEntity<Void> toggleDevice(@PathVariable("deviceId") String deviceId, @PathVariable("deviceSecret") String deviceSecret) {
        deviceV2WebsocketService.executeAction(deviceId, deviceSecret, "toggle");
        return ResponseEntity.ok(null);
    }
}