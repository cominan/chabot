package com.hung.chatbot.service;

import com.hung.chatbot.DTO.AuthRequest;
import com.hung.chatbot.DTO.AuthResponse;
import com.hung.chatbot.DTO.RegisterRequest;
import com.hung.chatbot.entity.User;
import com.hung.chatbot.model.UserInfo;
import com.hung.chatbot.repository.UserInfoRepository;
import com.hung.chatbot.repository.UserRepository;
import com.hung.chatbot.security.JwtUtils;
import com.hung.chatbot.security.UserDetailsImpl;
import com.hung.chatbot.service.TokenService.TokenType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private SessionRegistry sessionRegistry;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private TokenService tokenService;

    @Transactional
    public AuthResponse authenticateUser(AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Force logout existing web sessions (if any)
        expireExistingSessions(authentication);
        
        User user = userRepository.findByUsername(authRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found with username: " + authRequest.getUsername()));

        // Revoke previous tokens of this user
        tokenService.revokeActiveTokens(user, TokenType.ACCESS);
        tokenService.revokeActiveTokens(user, TokenType.REFRESH);
        tokenService.cleanUpExpiredTokens(user);

        String jwt = jwtUtils.generateJwtToken(authentication);
        String refreshToken = jwtUtils.generateRefreshToken(user.getUsername());

        // Persist newly issued tokens for revocation tracking
        tokenService.storeToken(user, jwt, TokenType.ACCESS);
        tokenService.storeToken(user, refreshToken, TokenType.REFRESH);

        return new AuthResponse(jwt, refreshToken, user);
    }

    private void expireExistingSessions(Authentication authentication) {
        if (sessionRegistry == null) {
            return;
        }

        for (SessionInformation sessionInformation : sessionRegistry.getAllSessions(authentication.getPrincipal(), false)) {
            sessionInformation.expireNow();

            if (webSocketService != null) {
                String username = authentication.getName();
                webSocketService.sendForceLogout(username, sessionInformation.getSessionId());
            }
        }
    }

    @Transactional
    public AuthResponse registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new IllegalArgumentException("Username is already taken!");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Email is already in use!");
        }

//        // Create new user
//        User user = registerRequest.toUser();
//        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
//
//        // Save user first to get the generated ID
//        user = userRepository.save(user);
//
//        // Create and save user info
//        UserInfo userInfo = registerRequest.toUserInfo(user);
//        userInfoRepository.save(userInfo);
//
//        // Generate tokens
//        String jwt = jwtUtils.generateTokenFromUsername(user.getUsername(), 60000);
//        String refreshToken = jwtUtils.generateRefreshToken(user);
//
//        return new AuthResponse(jwt, refreshToken, user);
        User user = registerRequest.toUser();
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        userRepository.save(user);

// Load UserDetails từ user vừa tạo
        UserDetails userDetails = UserDetailsImpl.build(user);

// Tạo authenticated token với UserDetails
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        // Revoke any lingering tokens (shouldn't exist yet, but keeps state consistent)
        tokenService.revokeActiveTokens(user, TokenType.ACCESS);
        tokenService.revokeActiveTokens(user, TokenType.REFRESH);
        tokenService.cleanUpExpiredTokens(user);

        String jwt = jwtUtils.generateJwtToken(authentication);
        String refreshToken = jwtUtils.generateRefreshToken(user.getUsername());

        tokenService.storeToken(user, jwt, TokenType.ACCESS);
        tokenService.storeToken(user, refreshToken, TokenType.REFRESH);

// Trả về đúng thứ tự: accessToken, refreshToken, user
        return new AuthResponse(jwt, refreshToken, user);
    }
}
