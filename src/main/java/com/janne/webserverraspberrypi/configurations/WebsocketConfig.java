package com.janne.webserverraspberrypi.configurations;


import com.janne.webserverraspberrypi.Websockets.AudioWebsocketHandler;
import com.janne.webserverraspberrypi.Websockets.ServiceManagerWebsocket;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


@Configuration
@EnableWebSocket
public class WebsocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new AudioWebsocketHandler(), "/audio").setAllowedOrigins("*");
        registry.addHandler(new ServiceManagerWebsocket() , "/service_controller").setAllowedOrigins("*");
    }
}