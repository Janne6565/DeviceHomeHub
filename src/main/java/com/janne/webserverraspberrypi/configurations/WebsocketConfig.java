package com.janne.webserverraspberrypi.configurations;


import com.janne.webserverraspberrypi.websockets.AudioWebsocketHandler;
import com.janne.webserverraspberrypi.websockets.ServiceManagerWebsocket;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


@Configuration
@EnableWebSocket
public class WebsocketConfig implements WebSocketConfigurer {
    private final AudioWebsocketHandler audioWebsocketHandler;
    private final ServiceManagerWebsocket serviceManagerWebsocket;

    public WebsocketConfig(AudioWebsocketHandler audioWebsocketHandler, ServiceManagerWebsocket serviceManagerWebsocket) {
        this.audioWebsocketHandler = audioWebsocketHandler;
        this.serviceManagerWebsocket = serviceManagerWebsocket;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(audioWebsocketHandler, "/audio").setAllowedOrigins("*");
        registry.addHandler(serviceManagerWebsocket , "/service_controller").setAllowedOrigins("*");
    }
}