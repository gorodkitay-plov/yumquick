package com.yumquick.delivery;

import com.yumquick.common.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class TrackingController {

    private final SimpMessagingTemplate messagingTemplate;
    private final DeliveryRepository deliveryRepository;

    // Курьер отправляет свои координаты
    // Клиент отправляет на: /app/tracking/{orderId}
    // Пользователи подписаны на: /topic/tracking/{orderId}

    @MessageMapping("/tracking/{orderId}")
    public void updateLocation(
            @DestinationVariable String orderId,
            DeliveryDto.LocationUpdateRequest request) {

        Delivery delivery = deliveryRepository.findByOrderId(UUID.fromString(orderId))
                .orElseThrow(() -> AppException.notFound("Delivery not found"));

        // Обновляем координаты в БД
        delivery.updateLocation(request.getLat(), request.getLng());
        deliveryRepository.save(delivery);

        // Рассылаем всем подписчикам этого заказа
        TrackingMessage message = TrackingMessage.from(delivery);
        messagingTemplate.convertAndSend("/topic/tracking/" + orderId, message);

        log.debug("Location broadcast: orderId={}, lat={}, lng={}",
                orderId, request.getLat(), request.getLng());
    }
}