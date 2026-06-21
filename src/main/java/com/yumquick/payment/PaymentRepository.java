package com.yumquick.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByOrderId(UUID orderId);

    // Проверка идемпотентности — не допускаем двойную оплату
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    // Поиск по ID транзакции провайдера (для webhook)
    Optional<Payment> findByProviderTxId(String providerTxId);
}
