package com.yumquick.menu;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "option_groups", indexes = {
        @Index(name = "idx_option_groups_menu_item_id", columnList = "menu_item_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OptionGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @Column(nullable = false)
    private String name;

    @Column(name = "is_required", nullable = false)
    private boolean required = false;

    @Column(name = "max_select", nullable = false)
    private int maxSelect = 1;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @OneToMany(mappedBy = "optionGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<MenuOption> options = new ArrayList<>();

    // ── Factory ───────────────────────────────────────────

    public static OptionGroup create(MenuItem menuItem, String name,
                                     boolean required, int maxSelect, int sortOrder) {
        OptionGroup g = new OptionGroup();
        g.menuItem = menuItem;
        g.name = name;
        g.required = required;
        g.maxSelect = maxSelect;
        g.sortOrder = sortOrder;
        return g;
    }

    public void update(String name, boolean required, int maxSelect) {
        if (name != null) this.name = name;
        this.required = required;
        this.maxSelect = maxSelect;
    }
}
