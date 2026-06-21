package com.yumquick.promo;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "coupons", indexes = {
        @Index(name = "idx_coupons_code", columnList = "code", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Код купона (например: SUMMER20)
    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType;

    // Значение скидки: для PERCENTAGE — 20 (=20%), для FIXED — 5000 (=5000 вон)
    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    // Минимальная сумма заказа для активации купона
    @Column(name = "min_order_amount", precision = 10, scale = 2)
    private BigDecimal minOrderAmount;

    // Максимальный размер скидки (для PERCENTAGE)
    @Column(name = "max_discount_amount", precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;

    // Лимит использований (null = безлимитный)
    @Column(name = "usage_limit")
    private Integer usageLimit;

    // Текущее количество использований
    @Column(name = "usage_count", nullable = false)
    private int usageCount = 0;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ── Factory ───────────────────────────────────────────

    public static Coupon create(String code, String description, DiscountType discountType,
                                BigDecimal discountValue, BigDecimal minOrderAmount,
                                BigDecimal maxDiscountAmount, Integer usageLimit,
                                LocalDateTime expiresAt) {
        Coupon coupon = new Coupon();
        coupon.code = code.toUpperCase();
        coupon.description = description;
        coupon.discountType = discountType;
        coupon.discountValue = discountValue;
        coupon.minOrderAmount = minOrderAmount;
        coupon.maxDiscountAmount = maxDiscountAmount;
        coupon.usageLimit = usageLimit;
        coupon.expiresAt = expiresAt;
        return coupon;
    }

    // ── Валидация и применение ────────────────────────────

    public boolean isValid() {
        if (!active) return false;
        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) return false;
        if (usageLimit != null && usageCount >= usageLimit) return false;
        return true;
    }

    // Рассчитать размер скидки для конкретного заказа
    public BigDecimal calculateDiscount(BigDecimal subtotal, BigDecimal deliveryFee) {
        return switch (discountType) {
            case PERCENTAGE -> {
                BigDecimal discount = subtotal.multiply(discountValue)
                        .divide(BigDecimal.valueOf(100));
                // Ограничиваем максимальной скидкой если задана
                if (maxDiscountAmount != null && discount.compareTo(maxDiscountAmount) > 0) {
                    yield maxDiscountAmount;
                }
                yield discount;
            }
            case FIXED -> discountValue.min(subtotal); // не больше суммы заказа
            case FREE_DELIVERY -> deliveryFee;
        };
    }

    public void incrementUsage() {
        this.usageCount++;
    }

    public void deactivate() { this.active = false; }
}
