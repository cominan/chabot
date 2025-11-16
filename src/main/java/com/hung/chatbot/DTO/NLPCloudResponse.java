package com.hung.chatbot.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * @author Admin
 * @since 11/16/2025
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NLPCloudResponse {
    private String response;

}