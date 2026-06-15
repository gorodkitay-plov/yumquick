package com.yumquick.order;

import com.yumquick.restaurant.Restaurant;
import com.yumquick.user.User;
import com.yumquick.user.UserAddress;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_user_id", columnList = "user_id"),
        @Index(name = "idx_orders_restaurant_id", columnList = "restaurant_id"),
        @Index(name = "idx_orders_status", columnList = "status"),
        @Index(name = "idx_orders_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    // Снапшот адреса — изменение адреса не влияет на старые заказы
    @Column(name = "delivery_address", nullable = false, columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "delivery_lat", nullable = false)
    private Double deliveryLat;

    @Column(name = "delivery_lng", nullable = false)
    private Double deliveryLng;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "delivery_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryFee;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Factory ───────────────────────────────────────────

    public static Order create(User user, Restaurant restaurant,
                               UserAddress address, BigDecimal subtotal,
                               BigDecimal deliveryFee, BigDecimal discount,
                               String notes) {
        Order order = new Order();
        order.user = user;
        order.restaurant = restaurant;
        order.deliveryAddress = address.getDetailAddress();
        order.deliveryLat = address.getLat();
        order.deliveryLng = address.getLng();
        order.subtotal = subtotal;
        order.deliveryFee = deliveryFee;
        order.discount = discount;
        order.total = subtotal.add(deliveryFee).subtract(discount);
        order.notes = notes;
        return order;
    }

    // ── Status transitions ────────────────────────────────

    public void confirm() {
        validateTransition(OrderStatus.CONFIRMED);
        this.status = OrderStatus.CONFIRMED;
    }

    public void startPreparing() {
        validateTransition(OrderStatus.PREPARING);
        this.status = OrderStatus.PREPARING;
    }

    public void readyForPickup() {
        validateTransition(OrderStatus.READY_FOR_PICKUP);
        this.status = OrderStatus.READY_FOR_PICKUP;
    }

    public void onTheWay() {
        validateTransition(OrderStatus.ON_THE_WAY);
        this.status = OrderStatus.ON_THE_WAY;
    }

    public void deliver() {
        validateTransition(OrderStatus.DELIVERED);
        this.status = OrderStatus.DELIVERED;
    }

    public void cancel() {
        if (this.status == OrderStatus.ON_THE_WAY || this.status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel order in status: " + this.status);
        }
        this.status = OrderStatus.CANCELLED;
    }

    private void validateTransition(OrderStatus next) {
        boolean valid = switch (next) {
            case CONFIRMED -> status == OrderStatus.PENDING;
            case PREPARING -> status == OrderStatus.CONFIRMED;
            case READY_FOR_PICKUP -> status == OrderStatus.PREPARING;
            case ON_THE_WAY -> status == OrderStatus.READY_FOR_PICKUP;
            case DELIVERED -> status == OrderStatus.ON_THE_WAY;
            default -> false;
        };
        if (!valid) {
            throw new IllegalStateException(
                    "Invalid status transition: " + status + " → " + next);
        }
    }
}
