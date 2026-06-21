package com.yumquick.promo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {

    Optional<Coupon> findByCodeAndActiveTrue(String code);

    Page<Coupon> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
