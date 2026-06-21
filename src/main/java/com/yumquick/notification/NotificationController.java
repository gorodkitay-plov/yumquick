package com.yumquick.notification;

import com.yumquick.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get notification history")
    public ResponseEntity<ApiResponse<Page<NotificationDto.NotificationResponse>>> getAll(
            @AuthenticationPrincipal UUID userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                notificationService.getNotifications(userId, pageable)));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notifications count")
    public ResponseEntity<ApiResponse<NotificationDto.UnreadCountResponse>> getUnreadCount(
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(
                notificationService.getUnreadCount(userId)));
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID notificationId) {
        notificationService.markAsRead(userId, notificationId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal UUID userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/tokens")
    @Operation(summary = "Register FCM device token")
    public ResponseEntity<ApiResponse<Void>> registerToken(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody NotificationDto.RegisterTokenRequest request) {
        notificationService.registerToken(userId, request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/tokens/{token}")
    @Operation(summary = "Remove FCM device token (on logout)")
    public ResponseEntity<ApiResponse<Void>> removeToken(
            @PathVariable String token) {
        notificationService.removeToken(token);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
