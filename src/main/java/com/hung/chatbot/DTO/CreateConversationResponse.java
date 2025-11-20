package com.hung.chatbot.DTO;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * @author Admin
 * @since 11/20/2025
 */
@Data
public class CreateConversationResponse {
    private Long conversationId;
    private Long userId;
    private String title;
    private LocalDateTime createdAt;
    private String message; // Thông báo thành công
}
