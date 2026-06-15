package com.yumquick.cart;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    private UUID menuItemId;
    private String title;
    private BigDecimal unitPrice;
    private int quantity;
    private List<CartItemOption> options;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CartItemOption {
        private UUID optionId;
        private String name;
        private BigDecimal extraPrice;
    }

    public BigDecimal totalPrice() {
        BigDecimal optionsPrice = options == null ? BigDecimal.ZERO :
                options.stream()
                        .map(CartItemOption::getExtraPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        return unitPrice.add(optionsPrice).multiply(BigDecimal.valueOf(quantity));
    }
}
