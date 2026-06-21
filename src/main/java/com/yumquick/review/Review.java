package com.yumquick.review;

import com.yumquick.order.Order;
import com.yumquick.restaurant.Restaurant;
import com.yumquick.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "idx_reviews_restaurant_id", columnList = "restaurant_id"),
        @Index(name = "idx_reviews_user_id", columnList = "user_id"),
        @Index(name = "idx_reviews_order_id", columnList = "order_id", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Один заказ — один отзыв
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    // Оценка ресторана (1-5)
    @Column(name = "restaurant_rating", nullable = false)
    private int restaurantRating;

    // Оценка курьера (1-5, необязательно)
    @Column(name = "courier_rating")
    private Integer courierRating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    // Фото к отзыву
    @Column(name = "image_url")
    private String imageUrl;

    // Скрыт админом
    @Column(name = "is_hidden", nullable = false)
    private boolean hidden = false;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ── Factory ───────────────────────────────────────────

    public static Review create(Order order, User user, Restaurant restaurant,
                                int restaurantRating, Integer courierRating,
                                String comment) {
        Review review = new Review();
        review.order = order;
        review.user = user;
        review.restaurant = restaurant;
        review.restaurantRating = restaurantRating;
        review.courierRating = courierRating;
        review.comment = comment;
        return review;
    }

    public void updateImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void hide() { this.hidden = true; }
    public void show() { this.hidden = false; }
}
