package com.yumquick.auth;

import com.yumquick.common.response.ApiResponse;
import com.yumquick.security.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication endpoints")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/signup")
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse<AuthDto.TokenResponse>> signup(
            @Valid @RequestBody AuthDto.SignupRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Account created", authService.signup(request)));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<ApiResponse<AuthDto.TokenResponse>> login(
            @Valid @RequestBody AuthDto.LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<ApiResponse<AuthDto.TokenResponse>> refresh(
            @Valid @RequestBody AuthDto.RefreshRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.refresh(request)));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout current user")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal UUID userId,
            @RequestHeader("Authorization") String bearerToken) {
        String accessToken = bearerToken.substring(7);
        authService.logout(userId, accessToken);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
