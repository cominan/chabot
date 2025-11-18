package com.hung.chatbot.controller;

import com.hung.chatbot.DTO.AuthRequest;
import com.hung.chatbot.DTO.AuthResponse;
import com.hung.chatbot.DTO.ErrorResponse;
import com.hung.chatbot.DTO.RegisterRequest;
import com.hung.chatbot.security.JwtUtils;
import com.hung.chatbot.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody AuthRequest authRequest) {
        return ResponseEntity.ok(authService.authenticateUser(authRequest));
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

    @RestController
    @RequestMapping("/api/auth")
    @CrossOrigin(origins = "*", maxAge = 3600)
    public class OAuth2Controller {

        @GetMapping("/oauth2/authorization/google")
        public void redirectToGoogleOAuth2() {
            // This will be handled by Spring Security
        }
    }
}
