package com.hung.chatbot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author Admin
 * @since 11/16/2025
 */
@Configuration
public class ChatConfig {

    @Bean
    public WebClient nlpCloudClient() {
        return WebClient.builder()
                .baseUrl("https://api.nlpcloud.io/v1/gpu/gpt-oss-120b")
                .defaultHeader("Authorization", "Token 408e03b7457f171b47c3ebb10920b1bb108a2de5")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}