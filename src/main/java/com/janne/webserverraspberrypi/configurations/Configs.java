package com.janne.webserverraspberrypi.configurations;

import com.janne.webserverraspberrypi.websockets.AudioWebsocketHandler;
import com.janne.webserverraspberrypi.websockets.ServiceManagerWebsocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class Configs {

    @Bean
    public Logger logger() {
        return LoggerFactory.getLogger(AudioWebsocketHandler.class);
    }

    @Bean
    public AudioWebsocketHandler audioWebsocketHandler(Logger logger) {
        return new AudioWebsocketHandler(logger);
    }

    @Bean
    public ServiceManagerWebsocket serviceManagerWebsocket() {
        return new ServiceManagerWebsocket();
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .clientConnector(
                        new ReactorClientHttpConnector(
                                HttpClient.newConnection().keepAlive(false)
                        )
                )
                .build();
    }
}

