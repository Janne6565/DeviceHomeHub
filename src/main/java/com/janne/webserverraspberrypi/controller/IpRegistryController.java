package com.janne.webserverraspberrypi.controller;

import ch.qos.logback.core.joran.sanity.Pair;
import com.janne.webserverraspberrypi.services.deviceIpRegistry.IpRegistryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ipRegistry")
public class IpRegistryController {

    private final IpRegistryService ipRegistryService;

    @GetMapping("/{deviceId}")
    public ResponseEntity<String> getDeviceIp(@PathVariable String deviceId) {
        log.info("get device ip: {}", deviceId);
        return ResponseEntity.ok(ipRegistryService.getDeviceIp(deviceId));
    }

    @GetMapping("/set/{deviceId}")
    public ResponseEntity<String> setDeviceIp(@PathVariable String deviceId, @RequestParam("ip") String ip, @RequestParam("secret") String secret) {
        log.info("set device ip: {} (ip: {}; secret: {})", deviceId, ip, secret);
        try {
            ipRegistryService.setDeviceIp(deviceId, ip, secret);
            return ResponseEntity.ok(ipRegistryService.getDeviceIp(deviceId));
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/")
    public ResponseEntity<Set<Map.Entry<String, String>>> getEntries() {
        return ResponseEntity.ok(ipRegistryService.getEntries());
    }
}
