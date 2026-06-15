package com.yumquick.user;

import com.yumquick.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile and address management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    // ── Profile ───────────────────────────────────────────

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<UserDto.ProfileResponse>> getProfile(
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getProfile(userId)));
    }

    @PatchMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ApiResponse<UserDto.ProfileResponse>> updateProfile(
            @AuthenticationPrincipal UUID userId,
            @RequestBody UserDto.UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(userService.updateProfile(userId, request)));
    }

    // ── Addresses ─────────────────────────────────────────

    @GetMapping("/me/addresses")
    @Operation(summary = "Get all addresses")
    public ResponseEntity<ApiResponse<List<UserDto.AddressResponse>>> getAddresses(
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getAddresses(userId)));
    }

    @PostMapping("/me/addresses")
    @Operation(summary = "Add a new address")
    public ResponseEntity<ApiResponse<UserDto.AddressResponse>> addAddress(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody UserDto.AddAddressRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(userService.addAddress(userId, request)));
    }

    @PatchMapping("/me/addresses/{addressId}")
    @Operation(summary = "Update an address")
    public ResponseEntity<ApiResponse<UserDto.AddressResponse>> updateAddress(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID addressId,
            @RequestBody UserDto.UpdateAddressRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok(userService.updateAddress(userId, addressId, request)));
    }

    @DeleteMapping("/me/addresses/{addressId}")
    @Operation(summary = "Delete an address")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID addressId) {
        userService.deleteAddress(userId, addressId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PatchMapping("/me/addresses/{addressId}/default")
    @Operation(summary = "Set address as default")
    public ResponseEntity<ApiResponse<UserDto.AddressResponse>> setDefault(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID addressId) {
        return ResponseEntity.ok(
                ApiResponse.ok(userService.setDefaultAddress(userId, addressId)));
    }
}
