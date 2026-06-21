package com.yumquick.favorite;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {

    Page<Favorite> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Optional<Favorite> findByUserIdAndRestaurantId(UUID userId, UUID restaurantId);

    boolean existsByUserIdAndRestaurantId(UUID userId, UUID restaurantId);

    void deleteByUserIdAndRestaurantId(UUID userId, UUID restaurantId);
}
