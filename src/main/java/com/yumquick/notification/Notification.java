package com.yumquick.notification;

import com.yumquick.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notifications_user_id", columnList = "user_id"),
        @Index(name = "idx_notifications_is_read", columnList = "is_read"),
        @Index(name = "idx_notifications_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    // ID связанного объекта (orderId, restaurantId и т.д.)
    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ── Factory ───────────────────────────────────────────

    public static Notification create(User user, NotificationType type,
                                      String title, String body, UUID referenceId) {
        Notification n = new Notification();
        n.user = user;
        n.type = type;
        n.title = title;
        n.body = body;
        n.referenceId = referenceId;
        return n;
    }

    public void markAsRead() {
        this.read = true;
    }
}
