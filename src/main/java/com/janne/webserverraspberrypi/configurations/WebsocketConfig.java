package com.janne.webserverraspberrypi.configurations;


import com.janne.webserverraspberrypi.websockets.AudioWebsocketService;
import com.janne.webserverraspberrypi.websockets.ServiceManagerWebsocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebsocketConfig implements WebSocketConfigurer {
    private final AudioWebsocketService audioWebsocketService;
    private final ServiceManagerWebsocketService serviceManagerWebsocketService;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(audioWebsocketService, "/audio").setAllowedOrigins("*");
        registry.addHandler(serviceManagerWebsocketService, "/service_controller").setAllowedOrigins("*");
    }
}