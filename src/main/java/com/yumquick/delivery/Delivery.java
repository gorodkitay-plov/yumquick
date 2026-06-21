package com.yumquick.delivery;

import com.yumquick.order.Order;
import com.yumquick.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "deliveries", indexes = {
        @Index(name = "idx_deliveries_order_id", columnList = "order_id", unique = true),
        @Index(name = "idx_deliveries_courier_id", columnList = "courier_id"),
        @Index(name = "idx_deliveries_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id", nullable = false)
    private User courier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status = DeliveryStatus.ASSIGNED;

    // Текущие координаты курьера — обновляются в реальном времени
    @Column(name = "current_lat")
    private Double currentLat;

    @Column(name = "current_lng")
    private Double currentLng;

    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Factory ───────────────────────────────────────────

    public static Delivery create(Order order, User courier) {
        Delivery delivery = new Delivery();
        delivery.order = order;
        delivery.courier = courier;
        return delivery;
    }

    // ── Действия курьера ──────────────────────────────────

    public void pickup() {
        if (this.status != DeliveryStatus.ASSIGNED) {
            throw new IllegalStateException("Cannot pickup in status: " + this.status);
        }
        this.status = DeliveryStatus.PICKED_UP;
        this.pickedUpAt = LocalDateTime.now();
    }

    public void startDelivery() {
        if (this.status != DeliveryStatus.PICKED_UP) {
            throw new IllegalStateException("Cannot start delivery in status: " + this.status);
        }
        this.status = DeliveryStatus.ON_THE_WAY;
    }

    public void complete() {
        if (this.status != DeliveryStatus.ON_THE_WAY) {
            throw new IllegalStateException("Cannot complete in status: " + this.status);
        }
        this.status = DeliveryStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }

    // Обновление координат курьера (для live tracking)
    public void updateLocation(Double lat, Double lng) {
        this.currentLat = lat;
        this.currentLng = lng;
    }
}
