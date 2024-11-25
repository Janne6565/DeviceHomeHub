package com.janne.webserverraspberrypi.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
@RequiredArgsConstructor
public class RequestService {

    public String sendRequest(String method, String url, String urlParameters) {
        StringBuilder response = new StringBuilder();

        try {
            HttpURLConnection con = getHttpURLConnection(method, url, urlParameters);

            // Read the response
            int responseCode = con.getResponseCode();
            System.out.println("Response Code: " + responseCode); // Optional logging

            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return response.toString();
    }

    private HttpURLConnection getHttpURLConnection(String method, String url, String urlParameters) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // Set the request method (GET, POST, etc.)
        con.setRequestMethod(method);
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); // Change as needed

        // Enable input and output streams
        con.setDoOutput(true);

        // Send request parameters if it's a POST request
        if ("POST".equalsIgnoreCase(method) && urlParameters != null) {
            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.writeBytes(urlParameters);
                wr.flush();
            }
        }
        return con;
    }

}
