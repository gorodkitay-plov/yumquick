package com.yumquick.restaurant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface RestaurantHoursRepository extends JpaRepository<RestaurantHours, UUID> {

    List<RestaurantHours> findByRestaurantIdOrderByDayOfWeek(UUID restaurantId);

    @Modifying
    @Query("DELETE FROM RestaurantHours h WHERE h.restaurant.id = :restaurantId")
    void deleteAllByRestaurantId(UUID restaurantId);
}
