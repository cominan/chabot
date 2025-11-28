package com.hung.chatbot.controller;

import com.hung.chatbot.security.CustomAuthenticationSuccessHandler;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/session")
public class SessionController {

    @Autowired
    private CustomAuthenticationSuccessHandler authSuccessHandler;

    @GetMapping("/id")
    public String getSessionId(HttpSession session) {
        return session.getId();
    }

    @GetMapping("/logout/other")
    public String logoutOtherSessions(HttpSession session) {
        // This endpoint can be called when user wants to log out from other devices
        // Implementation would depend on your session management strategy
        return "Logged out from other sessions";
    }
}
