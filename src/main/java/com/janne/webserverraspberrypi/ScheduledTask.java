package com.janne.webserverraspberrypi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.janne.webserverraspberrypi.Websockets.AudioWebsocketHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Component
public class ScheduledTask {

    @Value("${paths}")
    private String[] filePaths;

    private Map<String, Object> lastSend = new HashMap<>();
    @Scheduled(fixedRate = 100)
    public void myScheduledMethod() {
        // Your code logic goes here
        Map<String, Object> completeResult = new HashMap<>();

        for (String filePath : filePaths) {
            completeResult.putAll(parseJson(filePath));
        }

        for (String key : completeResult.keySet()) {
            if (lastSend == null || !completeResult.get(key).equals(lastSend.get(key))) {
                AudioWebsocketHandler.sendInformation(key, completeResult.get(key).toString());
                lastSend.put(key, completeResult.get(key));
            }
        }
    }

    public static Map parseJson(String filePath) {
        Map resultMap = new HashMap<>();

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
}
