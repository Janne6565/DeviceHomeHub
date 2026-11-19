package com.janne.webserverraspberrypi.controller;

import com.janne.webserverraspberrypi.Util;
import com.janne.webserverraspberrypi.services.ambilight.AmbilightMusicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Home Assistant facing switch for the ambilight music mode (album cover colors on the
 * projector LEDs). GET and POST both work, like the other switches.
 */
@RestController
@RequestMapping("/ambilight/music")
@RequiredArgsConstructor
public class AmbilightMusicController {

    private final AmbilightMusicService ambilightMusicService;
    private final Util util;

    @RequestMapping(value = "/on", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> on(@RequestParam String password) {
        return respond(password, () -> ambilightMusicService.setEnabled(true));
    }

    @RequestMapping(value = "/off", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> off(@RequestParam String password) {
        return respond(password, () -> ambilightMusicService.setEnabled(false));
    }

    @RequestMapping(value = "/status", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> status(@RequestParam String password) {
        return respond(password, ambilightMusicService::isEnabled);
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
