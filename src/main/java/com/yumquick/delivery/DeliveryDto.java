package com.yumquick.delivery;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

public class DeliveryDto {

    // ── Requests ──────────────────────────────────────────

    @Getter
    public static class AssignRequest {
        @NotNull
        private UUID orderId;

        @NotNull
        private UUID courierId;
    }

    @Getter
    public static class LocationUpdateRequest {
        @NotNull
        private Double lat;

        @NotNull
        private Double lng;
    }

    // ── Responses ─────────────────────────────────────────

    public record DeliveryResponse(
            UUID id,
            UUID orderId,
            UUID courierId,
            String courierName,
            DeliveryStatus status,
            Double currentLat,
            Double currentLng,
            LocalDateTime pickedUpAt,
            LocalDateTime deliveredAt,
            LocalDateTime createdAt
    ) {
        public static DeliveryResponse from(Delivery delivery) {
            return new DeliveryResponse(
                    delivery.getId(),
                    delivery.getOrder().getId(),
                    delivery.getCourier().getId(),
                    delivery.getCourier().getName(),
                    delivery.getStatus(),
                    delivery.getCurrentLat(),
                    delivery.getCurrentLng(),
                    delivery.getPickedUpAt(),
                    delivery.getDeliveredAt(),
                    delivery.getCreatedAt()
            );
        }
    }

    // Для live tracking — только координаты
    public record LocationResponse(
            UUID deliveryId,
            Double lat,
            Double lng,
            DeliveryStatus status
    ) {
        public static LocationResponse from(Delivery delivery) {
            return new LocationResponse(
                    delivery.getId(),
                    delivery.getCurrentLat(),
                    delivery.getCurrentLng(),
                    delivery.getStatus()
            );
        }
    }
}
