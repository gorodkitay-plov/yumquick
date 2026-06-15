package com.yumquick.menu;

import com.yumquick.restaurant.Restaurant;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "menu_categories", indexes = {
        @Index(name = "idx_menu_categories_restaurant_id", columnList = "restaurant_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Column(nullable = false)
    private String name;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<MenuItem> items = new ArrayList<>();

    // ── Factory ───────────────────────────────────────────

    public static MenuCategory create(Restaurant restaurant, String name, int sortOrder) {
        MenuCategory c = new MenuCategory();
        c.restaurant = restaurant;
        c.name = name;
        c.sortOrder = sortOrder;
        return c;
    }

    public void update(String name, int sortOrder) {
        if (name != null) this.name = name;
        this.sortOrder = sortOrder;
    }

    public void deactivate() { this.active = false; }
}
