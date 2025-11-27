package com.hung.chatbot.service;

import com.hung.chatbot.event.WebSocketEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendForceLogout(String username, String sessionId) {
        WebSocketEvent event = new WebSocketEvent(
            WebSocketEvent.FORCE_LOGOUT,
            "This account has been logged in from another device.",
            sessionId
        );
        
        // Send to user's specific queue
        messagingTemplate.convertAndSendToUser(
            username,
            "/queue/force-logout",
            event
        );
    }
}
