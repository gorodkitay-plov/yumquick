package com.yumquick.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    // ── Create ────────────────────────────────────────────

    public String createAccessToken(UUID userId, String role) {
        return buildToken(userId.toString(), role,
                jwtProperties.getAccessTokenExpiration(), "access");
    }

    public String createRefreshToken(UUID userId) {
        return buildToken(userId.toString(), null,
                jwtProperties.getRefreshTokenExpiration(), "refresh");
    }

    private String buildToken(String subject, String role, long expiration, String type) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        JwtBuilder builder = Jwts.builder()
                .subject(subject)
                .claim("type", type)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey());

        if (role != null) {
            builder.claim("role", role);
        }

        return builder.compact();
    }

    // ── Parse ─────────────────────────────────────────────

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID getUserId(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public String getTokenType(String token) {
        return parseClaims(token).get("type", String.class);
    }

    // ── Validate ──────────────────────────────────────────

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT expired: {}", e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT: {}", e.getMessage());
        }
        return false;
    }

    public long getRefreshTokenExpirationMs() {
        return jwtProperties.getRefreshTokenExpiration();
    }
}
