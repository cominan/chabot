package com.hung.chatbot.repository;

import com.hung.chatbot.entity.User;
import com.hung.chatbot.entity.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    List<UserToken> findByUserAndTokenTypeAndRevokedFalse(User user, UserToken.TokenType tokenType);
    Optional<UserToken> findByTokenHash(String tokenHash);
    void deleteByUserAndExpiresAtBefore(User user, Instant threshold);
}
