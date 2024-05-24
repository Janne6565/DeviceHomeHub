package com.janne.webserverraspberrypi.services;

import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;

@Service
public class GameCoverService {
    @Value("${files.discord}")
    private String filePath;

    public void dumpGameCover(String gameCoverUrl) {
        JsonObject jsonObject = new JsonObject();
        if (gameCoverUrl != null && !gameCoverUrl.isEmpty()) {
            jsonObject.addProperty("album", gameCoverUrl);
        }
        System.out.println("Loading: " + gameCoverUrl);
        try {
            FileWriter fileWriter = new FileWriter(filePath);
            System.out.println("Saving to: " + filePath);
            fileWriter.write(jsonObject.toString());
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
