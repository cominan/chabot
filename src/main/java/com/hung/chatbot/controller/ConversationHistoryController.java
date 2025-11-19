package com.hung.chatbot.controller;

import com.hung.chatbot.DTO.ConversationHistoryDTO;
import com.hung.chatbot.DTO.ConversationListDTO;
import com.hung.chatbot.entity.User;
import com.hung.chatbot.repository.UserRepository;
import com.hung.chatbot.service.ConversationHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Admin
 * @since 11/16/2025
 */
@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class ConversationHistoryController {

    private final ConversationHistoryService conversationHistoryService;
    private final UserRepository userRepository;

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationListDTO>> getUserConversations() {
        Long userId = getCurrentUserId();
        List<ConversationListDTO> conversations = conversationHistoryService.getUserConversations(userId);
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<ConversationHistoryDTO> getConversationWithMessages(
            @PathVariable Long conversationId) {
        Long userId = getCurrentUserId();
        ConversationHistoryDTO conversation = conversationHistoryService.getConversationWithMessages(
                conversationId, userId);
        return ResponseEntity.ok(conversation);
    }

    @GetMapping("/conversations/full")
    public ResponseEntity<List<ConversationHistoryDTO>> getUserConversationsWithMessages() {
        Long userId = getCurrentUserId();
        List<ConversationHistoryDTO> conversations = conversationHistoryService.getUserConversationsWithMessages(userId);
        return ResponseEntity.ok(conversations);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getUserId();
    }
}