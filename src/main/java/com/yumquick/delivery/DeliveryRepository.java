package com.yumquick.delivery;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {

    Optional<Delivery> findByOrderId(UUID orderId);

    Optional<Delivery> findByOrderIdAndCourierId(UUID orderId, UUID courierId);

    // Активные доставки курьера
    List<Delivery> findByCourierIdAndStatusIn(UUID courierId, List<DeliveryStatus> statuses);

    // История доставок курьера
    Page<Delivery> findByCourierIdOrderByCreatedAtDesc(UUID courierId, Pageable pageable);
}
