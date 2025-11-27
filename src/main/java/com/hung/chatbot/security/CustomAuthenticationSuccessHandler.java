package com.hung.chatbot.security;

import com.hung.chatbot.service.WebSocketService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Transactional
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final Map<String, String> userSessionMap = new ConcurrentHashMap<>();
    
    @Autowired
    private WebSocketService webSocketService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      Authentication authentication) throws IOException, ServletException {
        
        String username = authentication.getName();
        String sessionId = request.getSession().getId();
        
        // Check if user already has an active session
        if (userSessionMap.containsKey(username)) {
            String existingSessionId = userSessionMap.get(username);
            if (!existingSessionId.equals(sessionId)) {
                // Notify the old session to logout
                webSocketService.sendForceLogout(username, existingSessionId);
            }
        }
        
        // Update the session map with the new session
        userSessionMap.put(username, sessionId);
        
        // Set session timeout (in seconds)
        request.getSession().setMaxInactiveInterval(60 * 60 * 24); // 24 hours
        
        // Continue with the default redirect
        response.sendRedirect("/dashboard");
    }
    
    public void removeUserSession(String username) {
        userSessionMap.remove(username);
    }
}
