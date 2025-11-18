package com.hung.chatbot.security.oauth2;

import com.hung.chatbot.entity.User;
import com.hung.chatbot.repository.UserRepository;
import com.hung.chatbot.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    // In CustomOAuth2UserService.java
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();

        CustomOAuth2User customOAuth2User = new CustomOAuth2User(oauth2User, provider);

        // Process or save user data
        processOAuth2User(customOAuth2User);

        // Return UserDetailsImpl instead of UserDetailsImpl
        return (OAuth2User) UserDetailsImpl.buildFromOAuth2User(customOAuth2User);
    }

    private User processOAuth2User(CustomOAuth2User oauth2User) {
        String email = oauth2User.getEmail();
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            return userOptional.get();
        } else {
            // Register new user
            User user = new User();
            user.setEmail(email);
            user.setUsername(email); // or generate a unique username
            user.setUsername(oauth2User.getFullName());
            user.setProvider(oauth2User.getProvider());
            user.setProviderId(oauth2User.getName()); // or another unique ID from OAuth2
            user.setRole(User.Role.USER);
            user.setPassword("N/A"); // OAuth2 users don't need a password

            return userRepository.save(user);
        }
    }
}