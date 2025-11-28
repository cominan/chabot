package com.hung.chatbot.controller;

import com.hung.chatbot.DTO.AuthRequest;
import com.hung.chatbot.DTO.AuthResponse;
import com.hung.chatbot.DTO.ErrorResponse;
import com.hung.chatbot.DTO.RegisterRequest;
import com.hung.chatbot.security.JwtUtils;
import com.hung.chatbot.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "password", required = false) String password,
            @RequestBody(required = false) AuthRequest authRequest,
            HttpServletResponse response) {
            
        // Handle both form data and JSON
        if (authRequest == null && username != null && password != null) {
            authRequest = new AuthRequest();
            authRequest.setUsername(username);
            authRequest.setPassword(password);
        }
        
        if (authRequest == null) {
            return ResponseEntity.badRequest().body("Invalid request");
        }
        
        try {
            AuthResponse authResponse = authService.authenticateUser(authRequest);

            // Set fresh cookies so browser immediately uses new tokens
            Cookie accessTokenCookie = new Cookie("accessToken", authResponse.getAccessToken());
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(false);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(24 * 60 * 60);

            Cookie refreshTokenCookie = new Cookie("refreshToken", authResponse.getRefreshToken());
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(false);
            refreshTokenCookie.setPath("/api/auth/refresh");
            refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60);

            response.addCookie(accessTokenCookie);
            response.addCookie(refreshTokenCookie);

            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Invalid username or password"));
        }
    }

//    @PostMapping("/signup")
//    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
//        AuthResponse response = authService.registerUser(registerRequest);
//        return ResponseEntity.ok(response);
//    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> registerUser(
            @Valid @RequestBody RegisterRequest registerRequest,
            HttpServletResponse response) {

        AuthResponse authResponse = authService.registerUser(registerRequest);

        // Lấy JWT và refresh token từ authResponse
        String jwt = authResponse.getAccessToken();
        String refreshToken = authResponse.getRefreshToken();

        // Set HttpOnly cookies
        Cookie accessTokenCookie = new Cookie("accessToken", jwt);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(false); // false cho localhost, true cho production
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(24 * 60 * 60); // 24 hours

        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false);
        refreshTokenCookie.setPath("/api/auth/refresh");
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(authResponse);
    }


    @RequestMapping("/api/auth")
    @CrossOrigin(origins = "*", maxAge = 3600)
    public class OAuth2Controller {

        @GetMapping("/oauth2/authorization/google")
        public void redirectToGoogleOAuth2() {
            // This will be handled by Spring Security
        }
    }

    @RestController
    @RequestMapping("/session")
    public class SessionController {
        @GetMapping("/id")
        public Map<String,String> getSessionId(HttpSession session) {
            return Collections.singletonMap("sessionId", session.getId());
        }
    }

}
