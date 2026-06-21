package com.yumquick.promo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CouponUsageRepository extends JpaRepository<CouponUsage, UUID> {

    // Проверка — использовал ли пользователь этот купон?
    boolean existsByCouponIdAndUserId(UUID couponId, UUID userId);
}
