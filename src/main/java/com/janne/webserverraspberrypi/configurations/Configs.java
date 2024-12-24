package com.janne.webserverraspberrypi.configurations;

import com.janne.webserverraspberrypi.websockets.AudioWebsocketService;
import com.janne.webserverraspberrypi.websockets.ServiceManagerWebsocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class Configs {

    @Bean
    public Logger logger() {
        return LoggerFactory.getLogger(AudioWebsocketService.class);
    }

    @Bean
    public ServiceManagerWebsocketService serviceManagerWebsocket() {
        return new ServiceManagerWebsocketService();
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

