package com.yumquick.notification;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

public class NotificationDto {

    // ── Requests ──────────────────────────────────────────

    @Getter
    public static class RegisterTokenRequest {
        @NotBlank
        private String token;

        private String deviceType; // ios / android
    }

    // ── Responses ─────────────────────────────────────────

    public record NotificationResponse(
            UUID id,
            NotificationType type,
            String title,
            String body,
            UUID referenceId,
            boolean read,
            LocalDateTime createdAt
    ) {
        public static NotificationResponse from(Notification n) {
            return new NotificationResponse(
                    n.getId(), n.getType(), n.getTitle(),
                    n.getBody(), n.getReferenceId(),
                    n.isRead(), n.getCreatedAt()
            );
        }
    }

    public record UnreadCountResponse(long count) {}
}
