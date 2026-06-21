package com.yumquick.promo;

import com.yumquick.common.exception.AppException;
import com.yumquick.order.Order;
import com.yumquick.user.User;
import com.yumquick.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final UserRepository userRepository;

    // ── Проверка купона (до оформления заказа) ────────────

    public CouponDto.ValidateResponse validate(UUID userId, CouponDto.ValidateRequest req) {
        Coupon coupon = findActiveByCode(req.getCode());

        // Минимальная сумма заказа
        if (coupon.getMinOrderAmount() != null
                && req.getSubtotal().compareTo(coupon.getMinOrderAmount()) < 0) {
            throw AppException.badRequest(
                    "Minimum order amount for this coupon is " + coupon.getMinOrderAmount());
        }

        // Пользователь уже использовал этот купон
        if (couponUsageRepository.existsByCouponIdAndUserId(coupon.getId(), userId)) {
            throw AppException.conflict("Coupon already used");
        }

        // Для FREE_DELIVERY deliveryFee передаём как 0 — точная сумма будет при checkout
        BigDecimal discountAmount = coupon.calculateDiscount(req.getSubtotal(), BigDecimal.ZERO);

        return new CouponDto.ValidateResponse(
                coupon.getCode(),
                coupon.getDiscountType(),
                discountAmount,
                coupon.getDescription()
        );
    }

    // ── Применить купон (вызывается из OrderService при checkout) ─────

    @Transactional
    public BigDecimal apply(UUID userId, String code, Order order, BigDecimal deliveryFee) {
        if (code == null) return BigDecimal.ZERO;

        Coupon coupon = findActiveByCode(code);

        if (!coupon.isValid()) {
            throw AppException.badRequest("Coupon is no longer valid");
        }

        if (couponUsageRepository.existsByCouponIdAndUserId(coupon.getId(), userId)) {
            throw AppException.conflict("Coupon already used");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> AppException.notFound("User not found"));

        BigDecimal discountAmount = coupon.calculateDiscount(order.getSubtotal(), deliveryFee);

        // Сохраняем факт использования
        CouponUsage usage = CouponUsage.create(coupon, user, order, discountAmount);
        couponUsageRepository.save(usage);

        // Увеличиваем счётчик
        coupon.incrementUsage();

        log.info("Coupon applied: code={}, userId={}, discount={}", code, userId, discountAmount);
        return discountAmount;
    }

    // ── Admin: управление купонами ────────────────────────

    @Transactional
    public CouponDto.CouponResponse create(CouponDto.CreateRequest req) {
        if (couponRepository.findByCodeAndActiveTrue(req.getCode().toUpperCase()).isPresent()) {
            throw AppException.conflict("Coupon code already exists");
        }

        Coupon coupon = Coupon.create(
                req.getCode(), req.getDescription(), req.getDiscountType(),
                req.getDiscountValue(), req.getMinOrderAmount(),
                req.getMaxDiscountAmount(), req.getUsageLimit(), req.getExpiresAt()
        );
        couponRepository.save(coupon);
        return CouponDto.CouponResponse.from(coupon);
    }

    public Page<CouponDto.CouponResponse> getAll(Pageable pageable) {
        return couponRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(CouponDto.CouponResponse::from);
    }

    @Transactional
    public void deactivate(UUID couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> AppException.notFound("Coupon not found"));
        coupon.deactivate();
    }

    // ── Helpers ───────────────────────────────────────────

    private Coupon findActiveByCode(String code) {
        return couponRepository.findByCodeAndActiveTrue(code.toUpperCase())
                .orElseThrow(() -> AppException.notFound("Coupon not found or expired"));
    }
}
