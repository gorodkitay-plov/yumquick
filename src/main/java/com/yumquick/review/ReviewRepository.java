package com.yumquick.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    // Отзывы ресторана (только видимые)
    Page<Review> findByRestaurantIdAndHiddenFalseOrderByCreatedAtDesc(
            UUID restaurantId, Pageable pageable);

    // Отзывы пользователя
    Page<Review> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    // Проверка — уже оставлял отзыв на этот заказ?
    boolean existsByOrderId(UUID orderId);

    // Проверка — заказ принадлежит пользователю?
    Optional<Review> findByOrderIdAndUserId(UUID orderId, UUID userId);

    // Средний рейтинг ресторана для пересчёта
    @Query("""
            SELECT AVG(r.restaurantRating)
            FROM Review r
            WHERE r.restaurant.id = :restaurantId
              AND r.hidden = false
            """)
    Double averageRatingByRestaurantId(UUID restaurantId);

    // Количество отзывов ресторана
    long countByRestaurantIdAndHiddenFalse(UUID restaurantId);

    // Все отзывы для модерации (Admin)
    Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
