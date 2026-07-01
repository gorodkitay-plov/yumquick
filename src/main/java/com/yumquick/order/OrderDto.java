package com.yumquick.order;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class OrderDto {

    // ── Requests ──────────────────────────────────────────

    @Getter
    public static class CheckoutRequest {
        @NotNull
        private UUID addressId;

        private String notes;

        private UUID couponId;  // Phase 4
    }

    @Getter
    public static class StatusUpdateRequest {
        @NotNull
        private OrderStatus status;
    }

    // ── Responses ─────────────────────────────────────────

    public record OrderResponse(
            UUID id,
            UUID restaurantId,
            String restaurantName,
            String deliveryAddress,
            Double deliveryLat,
            Double deliveryLng,
            OrderStatus status,
            BigDecimal subtotal,
            BigDecimal deliveryFee,
            BigDecimal discount,
            BigDecimal total,
            String notes,
            List<OrderItemResponse> items,
            LocalDateTime createdAt
    ) {
        public static OrderResponse from(Order order) {
            return new OrderResponse(
                    order.getId(),
                    order.getRestaurant().getId(),
                    order.getRestaurant().getName(),
                    order.getDeliveryAddress(),
                    order.getDeliveryLat(),
                    order.getDeliveryLng(),
                    order.getStatus(),
                    order.getSubtotal(),
                    order.getDeliveryFee(),
                    order.getDiscount(),
                    order.getTotal(),
                    order.getNotes(),
                    order.getItems().stream().map(OrderItemResponse::from).toList(),
                    order.getCreatedAt()
            );
        }
    }

    public record OrderItemResponse(
            UUID id,
            UUID menuItemId,
            String title,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal itemTotal,
            List<OrderItemOptionResponse> options
    ) {
        public static OrderItemResponse from(OrderItem item) {
            return new OrderItemResponse(
                    item.getId(),
                    item.getMenuItemId(),
                    item.getTitleSnapshot(),
                    item.getUnitPriceSnapshot(),
                    item.getQuantity(),
                    item.getItemTotal(),
                    item.getOptions().stream().map(OrderItemOptionResponse::from).toList()
            );
        }
    }

    public record OrderItemOptionResponse(String name, BigDecimal extraPrice) {
        public static OrderItemOptionResponse from(OrderItemOption o) {
            return new OrderItemOptionResponse(o.getOptionName(), o.getExtraPrice());
        }
    }
}
