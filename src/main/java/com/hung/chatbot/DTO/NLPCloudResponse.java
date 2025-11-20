package com.hung.chatbot.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

/**
 * @author Admin
 * @since 11/16/2025
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NLPCloudResponse {
    private String response;
    private List<HistoryItem> history;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HistoryItem {
        private String input;
        private String response;
    }
}