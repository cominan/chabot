package com.hung.chatbot.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

/**
 * @author Admin
 * @since 11/16/2025
 */
@Data
public class NLPCloudRequest {
    private String input;
    private String context;
    private List<HistoryItem> history;

    @Data
    @AllArgsConstructor
    public static class HistoryItem {
        private String input;
        private String response;
    }
}