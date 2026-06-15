package com.yumquick.menu;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {

    List<MenuItem> findByCategoryIdOrderBySortOrder(UUID categoryId);

    Optional<MenuItem> findByIdAndCategoryRestaurantId(UUID id, UUID restaurantId);

    // 레스토랑 전체 메뉴 (카테고리 포함) - N+1 방지
    @Query("""
            SELECT DISTINCT i FROM MenuItem i
            JOIN FETCH i.category c
            WHERE c.restaurant.id = :restaurantId
              AND c.active = true
              AND i.available = true
            ORDER BY c.sortOrder, i.sortOrder
            """)
    List<MenuItem> findAllByRestaurantId(UUID restaurantId);
}
