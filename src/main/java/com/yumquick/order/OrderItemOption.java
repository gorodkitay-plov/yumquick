package com.yumquick.order;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_item_options", indexes = {
        @Index(name = "idx_order_item_options_order_item_id", columnList = "order_item_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItemOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Column(name = "option_name", nullable = false)
    private String optionName;

    @Column(name = "extra_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal extraPrice;

    // ── Factory ───────────────────────────────────────────

    public static OrderItemOption create(OrderItem orderItem,
                                         String optionName, BigDecimal extraPrice) {
        OrderItemOption o = new OrderItemOption();
        o.orderItem = orderItem;
        o.optionName = optionName;
        o.extraPrice = extraPrice;
        return o;
    }
}
