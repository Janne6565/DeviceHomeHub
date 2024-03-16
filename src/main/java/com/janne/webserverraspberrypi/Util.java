package com.janne.webserverraspberrypi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class Util {

    private final BCryptPasswordEncoder passwordEncoder;

    public Util(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Value("${credentials.password-hash}")
    private String storedPasswordHash;

    public boolean checkPassword(String password) {
        return passwordEncoder.matches(password, storedPasswordHash);
    }
}
