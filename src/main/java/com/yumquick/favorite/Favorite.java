package com.yumquick.favorite;

import com.yumquick.restaurant.Restaurant;
import com.yumquick.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "favorites", indexes = {
        @Index(name = "idx_favorites_user_id", columnList = "user_id"),
        // Один пользователь не может добавить один ресторан дважды
        @Index(name = "idx_favorites_user_restaurant", columnList = "user_id, restaurant_id",
                unique = true)
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ── Factory ───────────────────────────────────────────

    public static Favorite create(User user, Restaurant restaurant) {
        Favorite favorite = new Favorite();
        favorite.user = user;
        favorite.restaurant = restaurant;
        return favorite;
    }
}
