package com.yumquick.promo;

import com.yumquick.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Coupons")
public class CouponController {

    private final CouponService couponService;

    // ── User: проверка купона ─────────────────────────────

    @PostMapping("/api/coupons/validate")
    @Operation(summary = "Validate coupon and get discount amount")
    public ResponseEntity<ApiResponse<CouponDto.ValidateResponse>> validate(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody CouponDto.ValidateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(couponService.validate(userId, request)));
    }

    // ── Admin: управление купонами ────────────────────────

    @PostMapping("/api/admin/coupons")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: create coupon")
    public ResponseEntity<ApiResponse<CouponDto.CouponResponse>> create(
            @Valid @RequestBody CouponDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(couponService.create(request)));
    }

    @GetMapping("/api/admin/coupons")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: get all coupons")
    public ResponseEntity<ApiResponse<Page<CouponDto.CouponResponse>>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(couponService.getAll(pageable)));
    }

    @DeleteMapping("/api/admin/coupons/{couponId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: deactivate coupon")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable UUID couponId) {
        couponService.deactivate(couponId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
