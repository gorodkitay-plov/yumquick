package com.yumquick.auth;

import com.yumquick.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.UUID;

public class AuthDto {

    // ── Request ───────────────────────────────────────────

    @Getter
    public static class SignupRequest {
        @NotBlank(message = "Name is required")
        private String name;

        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        private String email;

        @NotBlank(message = "Password is required")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$",
                message = "Password must be at least 8 characters and contain uppercase, lowercase, digit and special character"
        )
        private String password;
    }

    @Getter
    public static class LoginRequest {
        @NotBlank
        private String email;

        @NotBlank
        private String password;
    }

    @Getter
    public static class RefreshRequest {
        @NotBlank
        private String refreshToken;
    }

    // ── Response ──────────────────────────────────────────

    public record TokenResponse(
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresIn,
            UserInfo user
    ) {
        public static TokenResponse of(String accessToken, String refreshToken,
                                       long expiresIn, UserInfo user) {
            return new TokenResponse(accessToken, refreshToken, "Bearer", expiresIn, user);
        }
    }

    public record UserInfo(UUID id, String name, String email, Role role) {}
}
