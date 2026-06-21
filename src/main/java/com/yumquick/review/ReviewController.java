package com.yumquick.review;

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
@Tag(name = "Reviews")
public class ReviewController {

    private final ReviewService reviewService;

    // ── User ──────────────────────────────────────────────

    @PostMapping("/api/reviews")
    @Operation(summary = "Create a review for delivered order")
    public ResponseEntity<ApiResponse<ReviewDto.ReviewResponse>> create(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody ReviewDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(reviewService.create(userId, request)));
    }

    @GetMapping("/api/reviews/my")
    @Operation(summary = "Get my reviews")
    public ResponseEntity<ApiResponse<Page<ReviewDto.ReviewResponse>>> getMyReviews(
            @AuthenticationPrincipal UUID userId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                reviewService.getMyReviews(userId, pageable)));
    }

    // ── Public ────────────────────────────────────────────

    @GetMapping("/api/restaurants/{restaurantId}/reviews")
    @Operation(summary = "Get restaurant reviews")
    public ResponseEntity<ApiResponse<Page<ReviewDto.ReviewResponse>>> getRestaurantReviews(
            @PathVariable UUID restaurantId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                reviewService.getRestaurantReviews(restaurantId, pageable)));
    }

    // ── Admin ─────────────────────────────────────────────

    @GetMapping("/api/admin/reviews")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: get all reviews for moderation")
    public ResponseEntity<ApiResponse<Page<ReviewDto.ReviewResponse>>> getAllReviews(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.getAllReviews(pageable)));
    }

    @PatchMapping("/api/admin/reviews/{reviewId}/hide")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: hide review")
    public ResponseEntity<ApiResponse<Void>> hide(@PathVariable UUID reviewId) {
        reviewService.hideReview(reviewId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PatchMapping("/api/admin/reviews/{reviewId}/show")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: restore hidden review")
    public ResponseEntity<ApiResponse<Void>> show(@PathVariable UUID reviewId) {
        reviewService.showReview(reviewId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
