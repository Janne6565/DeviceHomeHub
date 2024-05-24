package com.janne.webserverraspberrypi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
public class Util {

    private final BCryptPasswordEncoder passwordEncoder;
    @Value("${credentials.password-hash}")
    private String storedPasswordHash;

    public Util(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public static Map<String, Object> parseJsonFile(String filePath) {
        Map<String, Object> resultMap = new HashMap<>();

        try {
            // Read JSON file content
            String jsonContent = new String(Files.readAllBytes(Paths.get(filePath)));

            // Parse JSON using Jackson ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();
            resultMap = objectMapper.readValue(jsonContent, Map.class);
        } catch (IOException e) {
            // Handle file reading or JSON parsing errors or file not found
        }

        return resultMap;
    }

    public boolean checkPassword(String password) {
        return passwordEncoder.matches(password, storedPasswordHash);
    }
}
