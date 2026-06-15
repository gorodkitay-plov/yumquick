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

    // Owner의 레스토랑 목록
    List<Restaurant> findByOwnerIdAndActiveTrue(UUID ownerId);

    Optional<Restaurant> findByIdAndActiveTrue(UUID id);

    // 검색
    Page<Restaurant> findByActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);

    // 오픈 중인 레스토랑
    Page<Restaurant> findByActiveTrueAndOpenTrue(Pageable pageable);

    // 근처 레스토랑 (Haversine 공식)
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

    // 인기 레스토랑 (평점 기준)
    Page<Restaurant> findByActiveTrueAndOpenTrueOrderByRatingDescRatingCountDesc(Pageable pageable);

    // Owner + id 확인 (권한 검증용)
    Optional<Restaurant> findByIdAndOwnerId(UUID id, UUID ownerId);
}
