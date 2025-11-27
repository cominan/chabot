package com.hung.chatbot.event;

public class WebSocketEvent {
    public static final String FORCE_LOGOUT = "FORCE_LOGOUT";
    
    private String type;
    private String message;
    private String sessionId;

    public WebSocketEvent() {
    }

    public WebSocketEvent(String type, String message, String sessionId) {
        this.type = type;
        this.message = message;
        this.sessionId = sessionId;
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
