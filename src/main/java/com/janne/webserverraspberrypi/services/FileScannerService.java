package com.janne.webserverraspberrypi.services;

import com.janne.webserverraspberrypi.websockets.AudioWebsocketService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.janne.webserverraspberrypi.Util.parseJsonFile;

@Service
public class FileScannerService {
    private final AudioWebsocketService audioWebsocketService;
    @Value("${paths}")
    private List<String> paths;
    @Value("${pathSources}")
    private List<String> pathSources;

    private Map<String, Object> lastSend = new HashMap<>();

    public FileScannerService(AudioWebsocketService audioWebsocketService) {
        this.audioWebsocketService = audioWebsocketService;
    }

    public void runScan() {
        float currentTime = System.currentTimeMillis();
        Map<String, Object> completeResult = new HashMap<>();
        Map<String, String> resultSources = new HashMap<>();
        for (int i = 0; i < paths.size(); i++) {
            String currentFile = paths.get(i);
            String currentSource = pathSources.get(i);
            Map<String, Object> parsedFile = parseJsonFile(currentFile);
            for (String key : parsedFile.keySet()) {
                completeResult.put(key, parsedFile.get(key).toString());
                resultSources.put(key, currentSource);
            }
        }

        for (String key : completeResult.keySet()) {
            if (lastSend == null || !completeResult.get(key).equals(lastSend.get(key))) {
                System.out.println("Found not send parameter: " + key + " " + completeResult.get(key) + " from source: " + resultSources.get(key));
                audioWebsocketService.sendInformation(key, completeResult.get(key).toString(), resultSources.get(key), currentTime);
                lastSend.put(key, completeResult.get(key));
            }
        }
    }
}
