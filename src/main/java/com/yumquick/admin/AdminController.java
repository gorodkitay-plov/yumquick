package com.yumquick.admin;

import com.yumquick.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin")
public class AdminController {

    private final AdminService adminService;

    // ── Dashboard ─────────────────────────────────────────

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard statistics")
    public ResponseEntity<ApiResponse<AdminDto.DashboardStats>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getDashboardStats()));
    }

    // ── Users ─────────────────────────────────────────────

    @GetMapping("/users")
    @Operation(summary = "Get all users")
    public ResponseEntity<ApiResponse<Page<AdminDto.UserSummary>>> getUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getUsers(pageable)));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get user details")
    public ResponseEntity<ApiResponse<AdminDto.UserSummary>> getUser(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getUser(userId)));
    }

    @PatchMapping("/users/{userId}/role")
    @Operation(summary = "Change user role")
    public ResponseEntity<ApiResponse<AdminDto.UserSummary>> changeRole(
            @PathVariable UUID userId,
            @RequestBody AdminDto.ChangeRoleRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.changeRole(userId, request)));
    }

    @PatchMapping("/users/{userId}/ban")
    @Operation(summary = "Ban user")
    public ResponseEntity<ApiResponse<Void>> banUser(@PathVariable UUID userId) {
        adminService.banUser(userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PatchMapping("/users/{userId}/unban")
    @Operation(summary = "Unban user")
    public ResponseEntity<ApiResponse<Void>> unbanUser(@PathVariable UUID userId) {
        adminService.unbanUser(userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // ── Restaurants ───────────────────────────────────────

    @PatchMapping("/restaurants/{restaurantId}/toggle")
    @Operation(summary = "Activate or deactivate restaurant")
    public ResponseEntity<ApiResponse<Void>> toggleRestaurant(
            @PathVariable UUID restaurantId,
            @RequestBody AdminDto.ToggleRestaurantRequest request) {
        adminService.toggleRestaurantActive(restaurantId, request.isActive());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
