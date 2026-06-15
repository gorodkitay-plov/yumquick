package com.yumquick.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class CartDto {

    // ── Requests ──────────────────────────────────────────

    @Getter
    public static class AddItemRequest {
        @NotNull
        private UUID restaurantId;

        @NotNull
        private UUID menuItemId;

        @Min(1)
        private int quantity;

        private List<OptionRequest> options;
    }

    @Getter
    public static class UpdateQuantityRequest {
        @NotNull
        private UUID menuItemId;

        @Min(0)
        private int quantity;  // 0 = удалить
    }

    @Getter
    public static class OptionRequest {
        private UUID optionId;
        private String name;
        private BigDecimal extraPrice;
    }

    // ── Responses ─────────────────────────────────────────

    public record CartResponse(
            UUID restaurantId,
            String restaurantName,
            List<CartItemResponse> items,
            BigDecimal subtotal,
            int totalQuantity
    ) {
        public static CartResponse from(Cart cart) {
            if (cart == null || cart.isEmpty()) {
                return new CartResponse(null, null, List.of(), BigDecimal.ZERO, 0);
            }
            return new CartResponse(
                    cart.getRestaurantId(),
                    cart.getRestaurantName(),
                    cart.getItems().stream().map(CartItemResponse::from).toList(),
                    cart.subtotal(),
                    cart.totalQuantity()
            );
        }
    }

    public record CartItemResponse(
            UUID menuItemId,
            String title,
            BigDecimal unitPrice,
            int quantity,
            List<OptionItemResponse> options,
            BigDecimal totalPrice
    ) {
        public static CartItemResponse from(CartItem item) {
            return new CartItemResponse(
                    item.getMenuItemId(),
                    item.getTitle(),
                    item.getUnitPrice(),
                    item.getQuantity(),
                    item.getOptions() == null ? List.of() :
                            item.getOptions().stream().map(OptionItemResponse::from).toList(),
                    item.totalPrice()
            );
        }
    }

    public record OptionItemResponse(UUID optionId, String name, BigDecimal extraPrice) {
        public static OptionItemResponse from(CartItem.CartItemOption o) {
            return new OptionItemResponse(o.getOptionId(), o.getName(), o.getExtraPrice());
        }
    }
}
