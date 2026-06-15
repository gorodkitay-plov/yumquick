package com.yumquick.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String PREFIX = "refresh:";

    private final StringRedisTemplate redisTemplate;

    public void save(UUID userId, String refreshToken, long ttlMillis) {
        // userId → refreshToken (для rotation и logout)
        redisTemplate.opsForValue().set(
                PREFIX + userId,
                refreshToken,
                Duration.ofMillis(ttlMillis)
        );
    }

    public boolean validate(UUID userId, String refreshToken) {
        String stored = redisTemplate.opsForValue().get(PREFIX + userId);
        return refreshToken.equals(stored);
    }

    public void delete(UUID userId) {
        redisTemplate.delete(PREFIX + userId);
    }
}
