package com.hung.chatbot.DTO;

import lombok.Data;

/**
 * @author Admin
 * @since 11/16/2025
 */
@Data
public class ChatResponse {
    private Long conversationId;
    private String botReply;
}