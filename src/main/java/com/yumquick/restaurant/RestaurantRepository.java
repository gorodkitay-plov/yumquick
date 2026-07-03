package com.yumquick.restaurant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    // Список ресторанов владельца
    List<Restaurant> findByOwnerIdAndActiveTrue(UUID ownerId);

    Optional<Restaurant> findByIdAndActiveTrue(UUID id);

    // Поиск
    Page<Restaurant> findByActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);

    // Открытые рестораны
    Page<Restaurant> findByActiveTrueAndOpenTrue(Pageable pageable);

    Page<Restaurant> findByActiveTrueAndOpenTrueAndCategory(RestaurantCategory category, Pageable pageable);


    // Ближайшие рестораны (формула Хаверсина)
    @Query(value = """
            SELECT r.* FROM restaurants r
            WHERE r.is_active = true
              AND r.is_open = true
              AND (
                6371 * ACOS(
                  COS(RADIANS(:lat)) * COS(RADIANS(r.lat)) *
                  COS(RADIANS(r.lng) - RADIANS(:lng)) +
                  SIN(RADIANS(:lat)) * SIN(RADIANS(r.lat))
                )
              ) <= :radiusKm
            ORDER BY (
                6371 * ACOS(
                  COS(RADIANS(:lat)) * COS(RADIANS(r.lat)) *
                  COS(RADIANS(r.lng) - RADIANS(:lng)) +
                  SIN(RADIANS(:lat)) * SIN(RADIANS(r.lat))
                )
            ) ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<Restaurant> findNearby(@Param("lat") double lat,
                                @Param("lng") double lng,
                                @Param("radiusKm") double radiusKm,
                                @Param("limit") int limit);

    // Популярные рестораны (по рейтингу)
    Page<Restaurant> findByActiveTrueAndOpenTrueOrderByRatingDescRatingCountDesc(Pageable pageable);

    // Проверка владельца + id (для авторизации)
    Optional<Restaurant> findByIdAndOwnerId(UUID id, UUID ownerId);
}
