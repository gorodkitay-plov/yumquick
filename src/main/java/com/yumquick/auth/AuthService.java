package com.yumquick.auth;

import com.yumquick.common.exception.AppException;
import com.yumquick.security.jwt.JwtProperties;
import com.yumquick.security.jwt.JwtTokenProvider;
import com.yumquick.security.jwt.TokenBlacklistService;
import com.yumquick.user.Role;
import com.yumquick.user.User;
import com.yumquick.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;

    // ── Signup ────────────────────────────────────────────

    @Transactional
    public AuthDto.TokenResponse signup(AuthDto.SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw AppException.conflict("Email already registered");
        }

        User user = User.create(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                Role.USER
        );
        userRepository.save(user);

        return issueTokens(user);
    }

    // ── Login ─────────────────────────────────────────────

    public AuthDto.TokenResponse login(AuthDto.LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> AppException.unauthorized("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw AppException.unauthorized("Invalid credentials");
        }

        if (!user.isActive()) {
            throw AppException.forbidden("Account is deactivated");
        }

        return issueTokens(user);
    }

    // ── Refresh ───────────────────────────────────────────

    @Transactional
    public AuthDto.TokenResponse refresh(AuthDto.RefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw AppException.unauthorized("Invalid refresh token");
        }

        if (!"refresh".equals(jwtTokenProvider.getTokenType(refreshToken))) {
            throw AppException.unauthorized("Not a refresh token");
        }

        UUID userId = jwtTokenProvider.getUserId(refreshToken);

        if (!refreshTokenService.validate(userId, refreshToken)) {
            // Возможная кража токена — удаляем все токены пользователя
            refreshTokenService.delete(userId);
            throw AppException.unauthorized("Refresh token reuse detected");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> AppException.notFound("User not found"));

        // Rotation: старый refresh blacklist, выдаём новую пару
        tokenBlacklistService.blacklist(refreshToken,
                jwtProperties.getRefreshTokenExpiration());

        return issueTokens(user);
    }

    // ── Logout ────────────────────────────────────────────

    @Transactional
    public void logout(UUID userId, String accessToken) {
        refreshTokenService.delete(userId);
        // Access token живёт недолго, но всё равно blacklist-им
        tokenBlacklistService.blacklist(accessToken,
                jwtProperties.getAccessTokenExpiration());
    }

    // ── Helper ────────────────────────────────────────────

    private AuthDto.TokenResponse issueTokens(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(), user.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        refreshTokenService.save(user.getId(), refreshToken,
                jwtProperties.getRefreshTokenExpiration());

        return AuthDto.TokenResponse.of(
                accessToken,
                refreshToken,
                jwtProperties.getAccessTokenExpiration(),
                new AuthDto.UserInfo(
                        user.getId(), user.getName(),
                        user.getEmail(), user.getRole()
                )
        );
    }
}
