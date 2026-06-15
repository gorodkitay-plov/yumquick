package com.yumquick.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    // История заказов пользователя
    Page<Order> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    // История заказов ресторана
    Page<Order> findByRestaurantIdOrderByCreatedAtDesc(UUID restaurantId, Pageable pageable);

    // Активные заказы ресторана (PENDING ~ READY_FOR_PICKUP)
    @Query("""
            SELECT o FROM Order o
            WHERE o.restaurant.id = :restaurantId
              AND o.status IN (
                com.yumquick.order.OrderStatus.PENDING,
                com.yumquick.order.OrderStatus.CONFIRMED,
                com.yumquick.order.OrderStatus.PREPARING,
                com.yumquick.order.OrderStatus.READY_FOR_PICKUP
              )
            ORDER BY o.createdAt ASC
            """)
    Page<Order> findActiveByRestaurantId(UUID restaurantId, Pageable pageable);

    // Проверка пользователя + id
    Optional<Order> findByIdAndUserId(UUID id, UUID userId);

    // Проверка ресторана + id
    Optional<Order> findByIdAndRestaurantId(UUID id, UUID restaurantId);
}
