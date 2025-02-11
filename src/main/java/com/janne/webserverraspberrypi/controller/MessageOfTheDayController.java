package com.janne.webserverraspberrypi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/messageOfTheDay")
public class MessageOfTheDayController {

    private String messageOfTheDay = "line1;line2";

    @GetMapping
    public ResponseEntity<String> getMessageOfTheDay() {
        return ResponseEntity.ok(messageOfTheDay);
    }

    @PostMapping
    public ResponseEntity<String> postMessageOfTheDay(@RequestBody String message) {
        messageOfTheDay = message;
        return ResponseEntity.ok(messageOfTheDay);
    }
}
