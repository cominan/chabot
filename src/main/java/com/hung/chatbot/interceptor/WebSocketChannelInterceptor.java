package com.hung.chatbot.interceptor;

import com.hung.chatbot.security.CustomAuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    @Autowired
    private CustomAuthenticationSuccessHandler authSuccessHandler;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        
        // Check for connection events
        if (accessor.getCommand() != null) {
            switch (accessor.getCommand()) {
                case CONNECT:
                    handleConnect(accessor);
                    break;
                case DISCONNECT:
                    handleDisconnect(accessor);
                    break;
                default:
                    break;
            }
        }
        
        return message;
    }
    
    private void handleConnect(StompHeaderAccessor accessor) {
        // You can validate session here if needed
    }
    
    private void handleDisconnect(StompHeaderAccessor accessor) {
        // Handle cleanup on disconnect if needed
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            String username = auth.getName();
            // Remove user from active sessions if needed
        }
    }
}
