package com.yumquick.order;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "order_items", indexes = {
        @Index(name = "idx_order_items_order_id", columnList = "order_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Снапшот — изменение цены в меню не влияет на заказ
    @Column(name = "menu_item_id", nullable = false)
    private UUID menuItemId;

    @Column(name = "title_snapshot", nullable = false)
    private String titleSnapshot;

    @Column(name = "unit_price_snapshot", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPriceSnapshot;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "item_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal itemTotal;

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemOption> options = new ArrayList<>();

    // ── Factory ───────────────────────────────────────────

    public static OrderItem create(Order order, UUID menuItemId, String title,
                                   BigDecimal unitPrice, int quantity) {
        OrderItem item = new OrderItem();
        item.order = order;
        item.menuItemId = menuItemId;
        item.titleSnapshot = title;
        item.unitPriceSnapshot = unitPrice;
        item.quantity = quantity;
        item.itemTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        return item;
    }

    public void addOption(String optionName, BigDecimal extraPrice) {
        OrderItemOption option = OrderItemOption.create(this, optionName, extraPrice);
        this.options.add(option);
        this.itemTotal = this.itemTotal.add(
                extraPrice.multiply(BigDecimal.valueOf(quantity)));
    }
}
