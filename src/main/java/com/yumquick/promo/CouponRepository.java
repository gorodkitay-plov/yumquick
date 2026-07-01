package com.yumquick.promo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {

    Optional<Coupon> findByCodeAndActiveTrue(String code);

    Page<Coupon> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT c FROM Coupon c WHERE c.active = true AND (c.expiresAt IS NULL OR c.expiresAt > :now)")
    List<Coupon> findAvailable(@Param("now") LocalDateTime now);
}
