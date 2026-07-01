package com.yumquick.promo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class CouponDto {

    // ── Requests ──────────────────────────────────────────

    @Getter
    public static class CreateRequest {
        @NotBlank
        private String code;

        @NotBlank
        private String description;

        @NotNull
        private DiscountType discountType;

        @NotNull
        @DecimalMin("0.01")
        private BigDecimal discountValue;

        private BigDecimal minOrderAmount;
        private BigDecimal maxDiscountAmount;
        private Integer usageLimit;
        private LocalDateTime expiresAt;
    }

    @Getter
    public static class ValidateRequest {
        @NotBlank
        private String code;

        @NotNull
        private BigDecimal subtotal;
    }

    // ── Responses ─────────────────────────────────────────

    public record CouponResponse(
            UUID id,
            String code,
            String description,
            DiscountType discountType,
            BigDecimal discountValue,
            BigDecimal minOrderAmount,
            BigDecimal maxDiscountAmount,
            Integer usageLimit,
            int usageCount,
            LocalDateTime expiresAt,
            boolean active
    ) {
        public static CouponResponse from(Coupon coupon) {
            return new CouponResponse(
                    coupon.getId(), coupon.getCode(), coupon.getDescription(),
                    coupon.getDiscountType(), coupon.getDiscountValue(),
                    coupon.getMinOrderAmount(), coupon.getMaxDiscountAmount(),
                    coupon.getUsageLimit(), coupon.getUsageCount(),
                    coupon.getExpiresAt(), coupon.isActive()
            );
        }
    }

    public record ValidateResponse(
            UUID couponId,
            String code,
            DiscountType discountType,
            BigDecimal discountAmount,
            String description
    ) {}
}
