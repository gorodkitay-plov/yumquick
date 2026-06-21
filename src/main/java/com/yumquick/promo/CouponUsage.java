package com.yumquick.promo;

import com.yumquick.order.Order;
import com.yumquick.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "coupon_usages", indexes = {
        @Index(name = "idx_coupon_usages_coupon_id", columnList = "coupon_id"),
        @Index(name = "idx_coupon_usages_user_id", columnList = "user_id"),
        // Один пользователь — один раз на купон
        @Index(name = "idx_coupon_usages_user_coupon", columnList = "user_id, coupon_id",
                unique = true)
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Размер скидки который был применён
    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ── Factory ───────────────────────────────────────────

    public static CouponUsage create(Coupon coupon, User user,
                                     Order order, BigDecimal discountAmount) {
        CouponUsage usage = new CouponUsage();
        usage.coupon = coupon;
        usage.user = user;
        usage.order = order;
        usage.discountAmount = discountAmount;
        return usage;
    }
}
