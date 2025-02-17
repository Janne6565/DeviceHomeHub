package com.janne.webserverraspberrypi.controller;

import com.janne.webserverraspberrypi.services.LocalDeviceV2Service;
import com.janne.webserverraspberrypi.util.BenqProjectorUtilitys;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/localDeviceV2Controller")
@RequiredArgsConstructor
public class LocalDeviceV2Controller {
    private final LocalDeviceV2Service localDeviceV2Service;

    @PostMapping("/start/{deviceId}/{deviceSecret}")
    public ResponseEntity<String> start(@PathVariable String deviceId, @PathVariable String deviceSecret) {
        return ResponseEntity.ok(localDeviceV2Service.startDevice(deviceId, deviceSecret));
    }

    @PostMapping("/stop/{deviceId}/{deviceSecret}")
    public ResponseEntity<String> stop(@PathVariable String deviceId, @PathVariable String deviceSecret) {
        return ResponseEntity.ok(localDeviceV2Service.stopDevice(deviceId, deviceSecret));
    }

    @PostMapping("/toggle/{deviceId}/{deviceSecret}")
    public ResponseEntity<String> toggle(@PathVariable String deviceId, @PathVariable String deviceSecret) {
        return ResponseEntity.ok(localDeviceV2Service.toggleDevice(deviceId, deviceSecret));
    }

    @PostMapping("/execute/{deviceId}/{deviceSecret}/{operation}")
    public ResponseEntity<String> executePost(@PathVariable String deviceId, @PathVariable String deviceSecret, @PathVariable String operation) {
        return ResponseEntity.ok(localDeviceV2Service.executeWriteAction(deviceId, deviceSecret, operation));
    }

    @GetMapping("/execute/{deviceId}/{deviceSecret}/{operation}")
    public ResponseEntity<String> executeGet(@PathVariable String deviceId, @PathVariable String deviceSecret, @PathVariable String operation) {
        return ResponseEntity.ok(localDeviceV2Service.executeReadAction(deviceId, deviceSecret, operation));
    }

    @GetMapping("/getBeamerStatus/{deviceId}/{deviceSecret}")
    public ResponseEntity<String> getBeamerStatus(@PathVariable String deviceId, @PathVariable String deviceSecret) {
        return ResponseEntity.ok(BenqProjectorUtilitys.parseStatusString(localDeviceV2Service.executeReadAction(deviceId, deviceSecret, "pow=?")));
    }

    @GetMapping("/status/{deviceId}/{deviceSecret}")
    public ResponseEntity<String> getStatus(@PathVariable String deviceId, @PathVariable String deviceSecret) {
        return ResponseEntity.ok(localDeviceV2Service.getDeviceStatus(deviceId, deviceSecret));
    }
}
