package com.yumquick.delivery;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TrackingMessage {

    private UUID orderId;
    private UUID courierId;
    private Double lat;
    private Double lng;
    private DeliveryStatus status;
    private LocalDateTime timestamp;

    public static TrackingMessage from(Delivery delivery) {
        return new TrackingMessage(
                delivery.getOrder().getId(),
                delivery.getCourier().getId(),
                delivery.getCurrentLat(),
                delivery.getCurrentLng(),
                delivery.getStatus(),
                LocalDateTime.now()
        );
    }
}