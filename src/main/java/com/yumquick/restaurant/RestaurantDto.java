package com.yumquick.restaurant;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class RestaurantDto {

    // ── Requests ──────────────────────────────────────────

    @Getter
    public static class CreateRequest {
        @NotBlank
        private String name;

        private String description;

        @NotNull
        private Double lat;

        @NotNull
        private Double lng;

        private String addressDetail;

        @NotNull
        @DecimalMin("0.0")
        private BigDecimal deliveryFee;

        @NotNull
        @DecimalMin("0.0")
        private BigDecimal minOrder;

        private Integer estimatedDeliveryMinutes;

        private List<HoursRequest> hours;
    }

    @Getter
    public static class UpdateRequest {
        private String name;
        private String description;
        private String addressDetail;
        private BigDecimal deliveryFee;
        private BigDecimal minOrder;
        private Integer estimatedDeliveryMinutes;
    }

    @Getter
    public static class HoursRequest {
        @NotNull
        private Integer dayOfWeek;   // 0=Mon ... 6=Sun
        private LocalTime openTime;
        private LocalTime closeTime;
        private boolean closed;
    }

    @Getter
    public static class NearbyRequest {
        @NotNull
        private Double lat;
        @NotNull
        private Double lng;
        private Double radiusKm = 3.0;
        private int limit = 20;
    }

    // ── Responses ─────────────────────────────────────────

    public record RestaurantResponse(
            UUID id,
            String name,
            String description,
            String logoUrl,
            String coverUrl,
            double rating,
            int ratingCount,
            BigDecimal deliveryFee,
            BigDecimal minOrder,
            Integer estimatedDeliveryMinutes,
            boolean open,
            RestaurantCategory category,
            Double lat,
            Double lng,
            String addressDetail,
            List<HoursResponse> hours
    ) {
        public static RestaurantResponse from(Restaurant r, List<RestaurantHours> hours) {
            return new RestaurantResponse(
                    r.getId(), r.getName(), r.getDescription(),
                    r.getLogoUrl(), r.getCoverUrl(),
                    r.getRating().doubleValue(), r.getRatingCount(),
                    r.getDeliveryFee(), r.getMinOrder(),
                    r.getEstimatedDeliveryMinutes(),
                    r.isOpen(), r.getCategory(), r.getLat(), r.getLng(), r.getAddressDetail(),
                    hours.stream().map(HoursResponse::from).toList()
            );
        }

        public static RestaurantResponse from(Restaurant r) {
            return from(r, List.of());
        }
    }

    public record HoursResponse(
            int dayOfWeek,
            LocalTime openTime,
            LocalTime closeTime,
            boolean closed
    ) {
        public static HoursResponse from(RestaurantHours h) {
            return new HoursResponse(
                    h.getDayOfWeek(), h.getOpenTime(),
                    h.getCloseTime(), h.isClosed()
            );
        }
    }
}
