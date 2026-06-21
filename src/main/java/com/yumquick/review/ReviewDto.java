package com.yumquick.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

public class ReviewDto {

    // ── Requests ──────────────────────────────────────────

    @Getter
    public static class CreateRequest {
        @NotNull
        private UUID orderId;

        @NotNull
        @Min(1) @Max(5)
        private Integer restaurantRating;

        @Min(1) @Max(5)
        private Integer courierRating;

        private String comment;
    }

    // ── Responses ─────────────────────────────────────────

    public record ReviewResponse(
            UUID id,
            UUID orderId,
            UUID userId,
            String userName,
            int restaurantRating,
            Integer courierRating,
            String comment,
            String imageUrl,
            boolean hidden,
            LocalDateTime createdAt
    ) {
        public static ReviewResponse from(Review review) {
            return new ReviewResponse(
                    review.getId(),
                    review.getOrder().getId(),
                    review.getUser().getId(),
                    review.getUser().getName(),
                    review.getRestaurantRating(),
                    review.getCourierRating(),
                    review.getComment(),
                    review.getImageUrl(),
                    review.isHidden(),
                    review.getCreatedAt()
            );
        }
    }
}
