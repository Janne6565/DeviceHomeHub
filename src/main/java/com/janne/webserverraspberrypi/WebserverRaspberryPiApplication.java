package com.janne.webserverraspberrypi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import se.michaelthelin.spotify.SpotifyApi;

@SpringBootApplication
@EnableScheduling
public class WebserverRaspberryPiApplication {

    public static void main(String[] args) {
		SpringApplication.run(WebserverRaspberryPiApplication.class, args);
	}


	private void checkSpotifyStatus() {
	}
}
