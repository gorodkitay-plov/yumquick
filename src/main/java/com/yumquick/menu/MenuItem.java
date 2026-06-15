package com.yumquick.menu;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "menu_items", indexes = {
        @Index(name = "idx_menu_items_category_id", columnList = "category_id"),
        @Index(name = "idx_menu_items_is_available", columnList = "is_available")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private MenuCategory category;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_available", nullable = false)
    private boolean available = true;

    @Column(name = "spicy_level")
    private Integer spicyLevel;  // 0-3

    @Column
    private Integer calories;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @OneToMany(mappedBy = "menuItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<OptionGroup> optionGroups = new ArrayList<>();

    // ── Factory ───────────────────────────────────────────

    public static MenuItem create(MenuCategory category, String title,
                                   String description, BigDecimal price, int sortOrder) {
        MenuItem item = new MenuItem();
        item.category = category;
        item.title = title;
        item.description = description;
        item.price = price;
        item.sortOrder = sortOrder;
        return item;
    }

    public void update(String title, String description, BigDecimal price,
                       Integer spicyLevel, Integer calories, int sortOrder) {
        if (title != null) this.title = title;
        if (description != null) this.description = description;
        if (price != null) this.price = price;
        if (spicyLevel != null) this.spicyLevel = spicyLevel;
        if (calories != null) this.calories = calories;
        this.sortOrder = sortOrder;
    }

    public void updateImage(String imageUrl) { this.imageUrl = imageUrl; }
    public void setAvailable(boolean available) { this.available = available; }
}
