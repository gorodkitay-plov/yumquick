package com.yumquick.delivery;

import com.yumquick.common.exception.AppException;
import com.yumquick.notification.NotificationService;
import com.yumquick.order.Order;
import com.yumquick.order.OrderRepository;
import com.yumquick.order.OrderStatus;
import com.yumquick.user.User;
import com.yumquick.user.UserRepository;
import com.yumquick.user.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    // ── Admin: назначить курьера ──────────────────────────

    @Transactional
    public DeliveryDto.DeliveryResponse assign(DeliveryDto.AssignRequest req) {
        Order order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> AppException.notFound("Order not found"));

        if (order.getStatus() != OrderStatus.READY_FOR_PICKUP) {
            throw AppException.badRequest("Order is not ready for pickup");
        }

        // Проверяем что курьер существует и имеет роль DELIVERY
        User courier = userRepository.findById(req.getCourierId())
                .orElseThrow(() -> AppException.notFound("Courier not found"));

        if (courier.getRole() != Role.DELIVERY) {
            throw AppException.badRequest("User is not a courier");
        }

        // Проверяем что доставка ещё не назначена
        deliveryRepository.findByOrderId(req.getOrderId()).ifPresent(d -> {
            throw AppException.conflict("Delivery already assigned for this order");
        });

        Delivery delivery = Delivery.create(order, courier);
        deliveryRepository.save(delivery);

        // Уведомляем курьера о новом заказе
        notificationService.sendToCourier(courier.getId(),
                "Новый заказ", "Вам назначен заказ #" + order.getId());

        log.info("Delivery assigned: orderId={}, courierId={}", order.getId(), courier.getId());
        return DeliveryDto.DeliveryResponse.from(delivery);
    }

    // ── Курьер: забрал заказ ─────────────────────────────

    @Transactional
    public DeliveryDto.DeliveryResponse pickup(UUID courierId, UUID orderId) {
        Delivery delivery = findCourierDelivery(orderId, courierId);
        delivery.pickup();

        // Переводим заказ в ON_THE_WAY
        delivery.getOrder().onTheWay();

        // Уведомляем пользователя
        notificationService.sendToUser(
                delivery.getOrder().getUser().getId(),
                "Курьер в пути", "Ваш заказ забран и едет к вам"
        );

        return DeliveryDto.DeliveryResponse.from(delivery);
    }

    // ── Курьер: обновить координаты ──────────────────────

    @Transactional
    public DeliveryDto.LocationResponse updateLocation(UUID courierId, UUID orderId,
                                                       DeliveryDto.LocationUpdateRequest req) {
        Delivery delivery = findCourierDelivery(orderId, courierId);
        delivery.updateLocation(req.getLat(), req.getLng());

        // Рассылаем координаты всем подписчикам через WebSocket
        messagingTemplate.convertAndSend(
                "/topic/tracking/" + orderId,
                TrackingMessage.from(delivery)
        );

        log.debug("Location updated: courierId={}, lat={}, lng={}",
                courierId, req.getLat(), req.getLng());

        return DeliveryDto.LocationResponse.from(delivery);
    }

    // ── Курьер: доставил заказ ────────────────────────────

    @Transactional
    public DeliveryDto.DeliveryResponse complete(UUID courierId, UUID orderId) {
        Delivery delivery = findCourierDelivery(orderId, courierId);
        delivery.complete();

        // Переводим заказ в DELIVERED
        delivery.getOrder().deliver();

        // Уведомляем пользователя
        notificationService.sendToUser(
                delivery.getOrder().getUser().getId(),
                "Заказ доставлен", "Приятного аппетита! Не забудьте оставить отзыв"
        );

        log.info("Delivery completed: orderId={}, courierId={}", orderId, courierId);
        return DeliveryDto.DeliveryResponse.from(delivery);
    }

    // ── Получение статуса доставки ────────────────────────

    public DeliveryDto.DeliveryResponse getByOrderId(UUID orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> AppException.notFound("Delivery not found"));
        return DeliveryDto.DeliveryResponse.from(delivery);
    }

    public DeliveryDto.LocationResponse getLocation(UUID orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> AppException.notFound("Delivery not found"));
        return DeliveryDto.LocationResponse.from(delivery);
    }

    // ── История доставок курьера ──────────────────────────

    public Page<DeliveryDto.DeliveryResponse> getCourierHistory(UUID courierId,
                                                                Pageable pageable) {
        return deliveryRepository
                .findByCourierIdOrderByCreatedAtDesc(courierId, pageable)
                .map(DeliveryDto.DeliveryResponse::from);
    }

    public List<DeliveryDto.DeliveryResponse> getActiveCourierDeliveries(UUID courierId) {
        return deliveryRepository.findByCourierIdAndStatusIn(
                        courierId,
                        List.of(DeliveryStatus.ASSIGNED, DeliveryStatus.PICKED_UP,
                                DeliveryStatus.ON_THE_WAY)
                )
                .stream()
                .map(DeliveryDto.DeliveryResponse::from)
                .toList();
    }

    // ── Helpers ───────────────────────────────────────────

    private Delivery findCourierDelivery(UUID orderId, UUID courierId) {
        return deliveryRepository.findByOrderIdAndCourierId(orderId, courierId)
                .orElseThrow(() -> AppException.notFound("Delivery not found"));
    }
}