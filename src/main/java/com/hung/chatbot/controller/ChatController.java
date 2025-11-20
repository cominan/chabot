package com.hung.chatbot.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hung.chatbot.DTO.ChatRequest;
import com.hung.chatbot.DTO.ChatResponse;
import com.hung.chatbot.DTO.CreateConversationRequest;
import com.hung.chatbot.DTO.CreateConversationResponse;
import com.hung.chatbot.entity.User;
import com.hung.chatbot.repository.UserRepository;
import com.hung.chatbot.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final UserRepository userRepository;

    @PostMapping("/send")
    public ChatResponse sendMessage(@RequestBody ChatRequest req) {
        try {
            return chatService.handleChat(req);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/create-chat")
    public ResponseEntity<CreateConversationResponse> createConversation(@RequestBody CreateConversationRequest request) {
        Long userId = getCurrentUserId();
        CreateConversationResponse response = chatService.createConversation(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getUserId();
    }

}
