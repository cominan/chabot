package com.hung.chatbot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.List;

/**
 * com.hung.chatbot.config.WebSocketConfig
 * Author hungpv51 - hungpv51@fpt.com
 * Date 11/27/2025 3:59 PM
 */

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple in-memory broker with heartbeat
        config.enableSimpleBroker("/topic", "/queue")
              .setTaskScheduler(heartBeatScheduler())
              .setHeartbeatValue(new long[] {10000, 10000}); // 10 seconds
              
        // Configure the application destination prefix
        config.setApplicationDestinationPrefixes("/app");
        
        // Configure the user destination prefix for user-specific messages
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .addInterceptors(httpSessionHandshakeInterceptor())
                .setAllowedOriginPatterns("*")
                .withSockJS()
                .setHeartbeatTime(10000); // 10 seconds
    }
    
    @Bean
    public HandshakeInterceptor httpSessionHandshakeInterceptor() {
        return new HttpSessionHandshakeInterceptor();
    }
    
    @Bean
    public TaskScheduler heartBeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("ws-heartbeat-thread-");
        scheduler.initialize();
        return scheduler;
    }
}