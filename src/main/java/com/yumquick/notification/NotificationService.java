package com.yumquick.notification;

import com.yumquick.common.exception.AppException;
import com.yumquick.user.User;
import com.yumquick.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final UserRepository userRepository;

    // ── Отправка уведомлений (внутренний API) ─────────────
    // Вызывается из DeliveryService, OrderService и т.д.

    @Transactional
    public void sendToUser(UUID userId, String title, String body) {
        send(userId, NotificationType.ORDER_CONFIRMED, title, body, null);
    }

    @Transactional
    public void sendToCourier(UUID courierId, String title, String body) {
        send(courierId, NotificationType.COURIER_ASSIGNED, title, body, null);
    }

    @Transactional
    public void send(UUID userId, NotificationType type,
                     String title, String body, UUID referenceId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("Cannot send notification — user not found: {}", userId);
            return;
        }

        // 1. Сохраняем уведомление в БД
        Notification notification = Notification.create(user, type, title, body, referenceId);
        notificationRepository.save(notification);

        // 2. Отправляем push через FCM
        sendPushNotification(userId, title, body);
    }

    // ── FCM push ──────────────────────────────────────────

    private void sendPushNotification(UUID userId, String title, String body) {
        List<FcmToken> tokens = fcmTokenRepository.findByUserId(userId);
        if (tokens.isEmpty()) {
            log.debug("No FCM tokens for user {}", userId);
            return;
        }

        tokens.forEach(fcmToken -> {
            try {
                // TODO: вызов Firebase Admin SDK
                // FirebaseMessaging.getInstance().send(
                //     Message.builder()
                //         .setToken(fcmToken.getToken())
                //         .setNotification(com.google.firebase.messaging.Notification.builder()
                //             .setTitle(title)
                //             .setBody(body)
                //             .build())
                //         .build()
                // );
                log.info("Push sent to token={}, title={}", fcmToken.getToken(), title);
            } catch (Exception e) {
                log.error("Failed to send push to token={}", fcmToken.getToken(), e);
                // Если токен невалиден — удаляем его
                if (e.getMessage() != null && e.getMessage().contains("UNREGISTERED")) {
                    fcmTokenRepository.deleteByToken(fcmToken.getToken());
                }
            }
        });
    }

    // ── FCM токены ────────────────────────────────────────

    @Transactional
    public void registerToken(UUID userId, NotificationDto.RegisterTokenRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> AppException.notFound("User not found"));

        // Если токен уже существует — обновляем userId (смена аккаунта на устройстве)
        fcmTokenRepository.findByToken(req.getToken()).ifPresentOrElse(
                existing -> existing.updateToken(req.getToken()),
                () -> fcmTokenRepository.save(
                        FcmToken.create(user, req.getToken(), req.getDeviceType()))
        );
    }

    @Transactional
    public void removeToken(String token) {
        fcmTokenRepository.deleteByToken(token);
    }

    // ── История уведомлений пользователя ─────────────────

    public Page<NotificationDto.NotificationResponse> getNotifications(UUID userId,
                                                                        Pageable pageable) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationDto.NotificationResponse::from);
    }

    public NotificationDto.UnreadCountResponse getUnreadCount(UUID userId) {
        return new NotificationDto.UnreadCountResponse(
                notificationRepository.countByUserIdAndReadFalse(userId)
        );
    }

    @Transactional
    public void markAsRead(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> AppException.notFound("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw AppException.forbidden("Not your notification");
        }

        notification.markAsRead();
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }
}
