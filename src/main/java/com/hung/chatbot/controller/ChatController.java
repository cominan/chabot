package com.hung.chatbot.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hung.chatbot.DTO.ChatRequest;
import com.hung.chatbot.DTO.ChatResponse;
import com.hung.chatbot.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Admin
 * @since 11/16/2025
 */

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping("/send")
    public ChatResponse sendMessage(@RequestBody ChatRequest req) {
        try {
            return chatService.handleChat(req);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
