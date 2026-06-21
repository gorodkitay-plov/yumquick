package com.yumquick.auth;

import com.yumquick.common.exception.AppException;
import com.yumquick.security.jwt.JwtProperties;
import com.yumquick.security.jwt.JwtTokenProvider;
import com.yumquick.security.jwt.TokenBlacklistService;
import com.yumquick.user.Role;
import com.yumquick.user.User;
import com.yumquick.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 테스트")
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtTokenProvider jwtTokenProvider;
    @Mock JwtProperties jwtProperties;
    @Mock RefreshTokenService refreshTokenService;
    @Mock TokenBlacklistService tokenBlacklistService;

    @InjectMocks AuthService authService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.create("Dmitriy", "test@test.com", "encodedPassword", Role.USER);
    }

    // ── Signup ────────────────────────────────────────────

    @Test
    @DisplayName("Успешная регистрация")
    void signup_success() {
        // given
        AuthDto.SignupRequest request = new AuthDto.SignupRequest();
        setField(request, "name", "Dmitriy");
        setField(request, "email", "test@test.com");
        setField(request, "password", "Test1234!");

        given(userRepository.existsByEmail("test@test.com")).willReturn(false);
        given(passwordEncoder.encode("Test1234!")).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(mockUser);
        given(jwtTokenProvider.createAccessToken(any(), anyString())).willReturn("accessToken");
        given(jwtTokenProvider.createRefreshToken(any())).willReturn("refreshToken");
        given(jwtProperties.getAccessTokenExpiration()).willReturn(900000L);
        given(jwtProperties.getRefreshTokenExpiration()).willReturn(2592000000L);

        // when
        AuthDto.TokenResponse response = authService.signup(request);

        // then
        assertThat(response.accessToken()).isEqualTo("accessToken");
        assertThat(response.refreshToken()).isEqualTo("refreshToken");
        assertThat(response.user().email()).isEqualTo("test@test.com");
        then(userRepository).should().save(any(User.class));
    }

    @Test
    @DisplayName("이미 사용 중인 이메일로 가입하면 409 에러")
    void signup_duplicateEmail_throwsConflict() {
        // given
        AuthDto.SignupRequest request = new AuthDto.SignupRequest();
        setField(request, "name", "Dmitriy");
        setField(request, "email", "test@test.com");
        setField(request, "password", "Test1234!");

        given(userRepository.existsByEmail("test@test.com")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Email already registered");
    }

    // ── Login ─────────────────────────────────────────────

    @Test
    @DisplayName("Успешный логин")
    void login_success() {
        // given
        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        setField(request, "email", "test@test.com");
        setField(request, "password", "Test1234!");

        given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(mockUser));
        given(passwordEncoder.matches("Test1234!", "encodedPassword")).willReturn(true);
        given(jwtTokenProvider.createAccessToken(any(), anyString())).willReturn("accessToken");
        given(jwtTokenProvider.createRefreshToken(any())).willReturn("refreshToken");
        given(jwtProperties.getAccessTokenExpiration()).willReturn(900000L);
        given(jwtProperties.getRefreshTokenExpiration()).willReturn(2592000000L);

        // when
        AuthDto.TokenResponse response = authService.login(request);

        // then
        assertThat(response.accessToken()).isEqualTo("accessToken");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인하면 401 에러")
    void login_userNotFound_throwsUnauthorized() {
        // given
        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        setField(request, "email", "notfound@test.com");
        setField(request, "password", "Test1234!");

        given(userRepository.findByEmail("notfound@test.com")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    @DisplayName("비밀번호가 틀리면 401 에러")
    void login_wrongPassword_throwsUnauthorized() {
        // given
        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        setField(request, "email", "test@test.com");
        setField(request, "password", "WrongPassword!");

        given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(mockUser));
        given(passwordEncoder.matches("WrongPassword!", "encodedPassword")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Invalid credentials");
    }

    // ── Logout ────────────────────────────────────────────

    @Test
    @DisplayName("로그아웃 시 토큰 블랙리스트 등록")
    void logout_blacklistsToken() {
        // given
        UUID userId = UUID.randomUUID();
        given(jwtProperties.getAccessTokenExpiration()).willReturn(900000L);

        // when
        authService.logout(userId, "accessToken");

        // then
        then(refreshTokenService).should().delete(userId);
        then(tokenBlacklistService).should().blacklist("accessToken", 900000L);
    }

    // ── Helper ────────────────────────────────────────────

    private void setField(Object obj, String fieldName, Object value) {
        try {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}