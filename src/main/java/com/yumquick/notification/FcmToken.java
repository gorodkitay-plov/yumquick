package com.yumquick.notification;

import com.yumquick.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fcm_tokens", indexes = {
        @Index(name = "idx_fcm_tokens_user_id", columnList = "user_id"),
        @Index(name = "idx_fcm_tokens_token", columnList = "token", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // FCM токен устройства (обновляется при каждом запуске приложения)
    @Column(nullable = false, unique = true, length = 512)
    private String token;

    // Тип устройства
    @Column(name = "device_type")
    private String deviceType; // ios / android

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Factory ───────────────────────────────────────────

    public static FcmToken create(User user, String token, String deviceType) {
        FcmToken fcmToken = new FcmToken();
        fcmToken.user = user;
        fcmToken.token = token;
        fcmToken.deviceType = deviceType;
        return fcmToken;
    }

    public void updateToken(String newToken) {
        this.token = newToken;
    }
}
