package com.yumquick.menu;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "menu_options", indexes = {
        @Index(name = "idx_menu_options_group_id", columnList = "option_group_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_group_id", nullable = false)
    private OptionGroup optionGroup;

    @Column(nullable = false)
    private String name;

    @Column(name = "extra_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal extraPrice = BigDecimal.ZERO;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    // ── Factory ───────────────────────────────────────────

    public static MenuOption create(OptionGroup group, String name,
                                    BigDecimal extraPrice, int sortOrder) {
        MenuOption o = new MenuOption();
        o.optionGroup = group;
        o.name = name;
        o.extraPrice = extraPrice;
        o.sortOrder = sortOrder;
        return o;
    }

    public void update(String name, BigDecimal extraPrice) {
        if (name != null) this.name = name;
        if (extraPrice != null) this.extraPrice = extraPrice;
    }
}
