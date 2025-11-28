package com.hung.chatbot.security;

import com.hung.chatbot.service.WebSocketService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@Component
@Transactional
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    
    private final SessionRegistry sessionRegistry;
    private final WebSocketService webSocketService;
    
    @Autowired
    public CustomAuthenticationSuccessHandler(SessionRegistry sessionRegistry, WebSocketService webSocketService) {
        this.sessionRegistry = sessionRegistry;
        this.webSocketService = webSocketService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      Authentication authentication) throws IOException, ServletException {
        
        String username = authentication.getName();
        String currentSessionId = request.getSession().getId();
        
        // Get all sessions for this user
        List<SessionInformation> sessions = sessionRegistry.getAllSessions(authentication.getPrincipal(), false);
        
        // Expire all other sessions except the current one
        for (SessionInformation session : sessions) {
            if (!session.getSessionId().equals(currentSessionId)) {
                // Notify the old session to logout via WebSocket
                webSocketService.sendForceLogout(username, session.getSessionId());
                // Expire the session
                session.expireNow();
            }
        }
        
        // Register the new session
        sessionRegistry.registerNewSession(currentSessionId, authentication.getPrincipal());
        
        // Set session timeout (in seconds)
        request.getSession().setMaxInactiveInterval(60 * 60 * 24); // 24 hours
        
        // Continue with the default redirect
        response.sendRedirect("/dashboard");
    }
    
    public void removeUserSession(String username) {
        // No need to implement this as SessionRegistry handles it
    }
}
