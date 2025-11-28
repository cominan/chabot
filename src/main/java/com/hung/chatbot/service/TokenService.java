package com.hung.chatbot.service;

import com.hung.chatbot.entity.User;
import com.hung.chatbot.security.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class TokenService {

    public enum TokenType {
        ACCESS,
        REFRESH
    }

    private enum TokenStatus {
        ACTIVE,
        REVOKED
    }

    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);
    private static final String TOKEN_STATUS_PREFIX = "token:status:";
    private static final String USER_TOKENS_PREFIX = "user:tokens:";

    private final StringRedisTemplate redisTemplate;
    private final JwtUtils jwtUtils;

    @Autowired
    public TokenService(StringRedisTemplate redisTemplate, JwtUtils jwtUtils) {
        this.redisTemplate = redisTemplate;
        this.jwtUtils = jwtUtils;
    }

    public void revokeActiveTokens(User user, TokenType tokenType) {
        String setKey = buildUserTokensKey(user.getUserId(), tokenType);
        Set<String> tokenHashes = redisTemplate.opsForSet().members(setKey);
        if (tokenHashes == null || tokenHashes.isEmpty()) {
            return;
        }

        for (String hash : tokenHashes) {
            String tokenKey = buildTokenStatusKey(hash);
            redisTemplate.opsForValue().set(tokenKey, TokenStatus.REVOKED.name());
            redisTemplate.opsForSet().remove(setKey, hash);
        }
    }

    public void storeToken(User user, String rawToken, TokenType tokenType) {
        String hash = jwtUtils.hashToken(rawToken);
        Instant expiresAt = jwtUtils.getExpirationInstant(rawToken);
        long ttlSeconds = Duration.between(Instant.now(), expiresAt).getSeconds();
        if (ttlSeconds <= 0) {
            ttlSeconds = 1;
        }

        String tokenKey = buildTokenStatusKey(hash);
        redisTemplate.opsForValue().set(tokenKey, TokenStatus.ACTIVE.name(), ttlSeconds, TimeUnit.SECONDS);

        String setKey = buildUserTokensKey(user.getUserId(), tokenType);
        redisTemplate.opsForSet().add(setKey, hash);
        Long currentTtl = redisTemplate.getExpire(setKey);
        if (currentTtl == null || currentTtl < ttlSeconds) {
            redisTemplate.expire(setKey, ttlSeconds, TimeUnit.SECONDS);
        }
    }

    public boolean isTokenRevokedOrExpired(String rawToken) {
        String hash = jwtUtils.hashToken(rawToken);
        String tokenKey = buildTokenStatusKey(hash);
        String status = redisTemplate.opsForValue().get(tokenKey);

        if (status == null) {
            logger.debug("Token hash {} not found in Redis -> treat as revoked", hash);
            return true;
        }

        boolean revoked = TokenStatus.REVOKED.name().equals(status);
        if (revoked) {
            logger.debug("Token hash {} marked as revoked", hash);
        }
        return revoked;
    }

    public void cleanUpExpiredTokens(User user) {
        for (TokenType type : TokenType.values()) {
            String setKey = buildUserTokensKey(user.getUserId(), type);
            Set<String> hashes = redisTemplate.opsForSet().members(setKey);
            if (hashes == null || hashes.isEmpty()) {
                continue;
            }

            for (String hash : hashes) {
                String tokenKey = buildTokenStatusKey(hash);
                if (Boolean.FALSE.equals(redisTemplate.hasKey(tokenKey))) {
                    redisTemplate.opsForSet().remove(setKey, hash);
                }
            }
        }
    }

    private String buildTokenStatusKey(String tokenHash) {
        return TOKEN_STATUS_PREFIX + tokenHash;
    }

    private String buildUserTokensKey(Long userId, TokenType tokenType) {
        return USER_TOKENS_PREFIX + userId + ":" + tokenType.name();
    }
}
