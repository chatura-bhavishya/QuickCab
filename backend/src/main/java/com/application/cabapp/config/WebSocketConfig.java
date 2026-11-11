package com.application.cabapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.application.cabapp.realtime.StompAuthInterceptor;
import com.application.cabapp.realtime.Topics;

/**
 * STOMP over a plain WebSocket at /ws, with Spring's in-memory broker.
 * (With several API instances this would switch to a shared broker such as
 * RabbitMQ or Redis so every instance sees every event.)
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthInterceptor stompAuthInterceptor;
    private final String[] allowedOrigins;

    public WebSocketConfig(StompAuthInterceptor stompAuthInterceptor,
                           @Value("${app.cors.allowed-origins}") String[] allowedOrigins) {
        this.stompAuthInterceptor = stompAuthInterceptor;
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOrigins(allowedOrigins);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker(Topics.PREFIX);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthInterceptor);
    }
}
