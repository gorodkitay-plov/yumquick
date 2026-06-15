package com.yumquick.restaurant;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "restaurant_hours", indexes = {
        @Index(name = "idx_restaurant_hours_restaurant_id", columnList = "restaurant_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RestaurantHours {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    // 0 = Monday ... 6 = Sunday
    @Column(name = "day_of_week", nullable = false)
    private int dayOfWeek;

    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;

    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    @Column(name = "is_closed", nullable = false)
    private boolean closed = false;

    // ── Factory ───────────────────────────────────────────

    public static RestaurantHours create(Restaurant restaurant, int dayOfWeek,
                                         LocalTime openTime, LocalTime closeTime) {
        RestaurantHours h = new RestaurantHours();
        h.restaurant = restaurant;
        h.dayOfWeek = dayOfWeek;
        h.openTime = openTime;
        h.closeTime = closeTime;
        return h;
    }

    public static RestaurantHours createClosed(Restaurant restaurant, int dayOfWeek) {
        RestaurantHours h = new RestaurantHours();
        h.restaurant = restaurant;
        h.dayOfWeek = dayOfWeek;
        h.closed = true;
        return h;
    }

    public void update(LocalTime openTime, LocalTime closeTime, boolean closed) {
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.closed = closed;
    }
}
