package com.janne.webserverraspberrypi.controller;

import com.janne.webserverraspberrypi.Util;
import com.janne.webserverraspberrypi.services.WifiQrService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Homebridge facing switch for the wifi QR code on the led matrix. Every route answers to
 * GET and POST because the http switch plugin issues GETs.
 */
@RestController
@RequestMapping("/wifiQr")
@RequiredArgsConstructor
public class WifiQrController {

    private final WifiQrService wifiQrService;
    private final Util util;

    @RequestMapping(value = "/on", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> on(@RequestParam String password) {
        return respond(password, () -> wifiQrService.setShown(true));
    }

    @RequestMapping(value = "/off", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> off(@RequestParam String password) {
        return respond(password, () -> wifiQrService.setShown(false));
    }

    @RequestMapping(value = "/toggle", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> toggle(@RequestParam String password) {
        return respond(password, wifiQrService::toggle);
    }

    @RequestMapping(value = "/status", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> status(@RequestParam String password) {
        return respond(password, wifiQrService::isShown);
    }

    private ResponseEntity<String> respond(String password, BooleanAction action) {
        if (!util.checkPassword(password)) {
            return new ResponseEntity<>("Password Mismatch", HttpStatus.UNAUTHORIZED);
        }

        return ResponseEntity.ok(action.run() ? "1" : "0");
    }

    private interface BooleanAction {
        boolean run();
    }
}
