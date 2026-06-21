package com.yumquick.payment;

import com.yumquick.common.exception.AppException;
import com.yumquick.order.Order;
import com.yumquick.order.OrderRepository;
import com.yumquick.order.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final TossPaymentClient tossClient;

    // ── Инициализация оплаты ──────────────────────────────
    // Шаг 1: создаём запись Payment и возвращаем idempotencyKey.
    // Фронт передаёт его Toss SDK как orderId при открытии формы оплаты.

    @Transactional
    public PaymentDto.PaymentInitResponse initPayment(UUID userId,
                                                      PaymentDto.InitRequest req) {
        Order order = orderRepository.findByIdAndUserId(req.getOrderId(), userId)
                .orElseThrow(() -> AppException.notFound("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw AppException.badRequest("Order is not awaiting payment");
        }

        // Если оплата уже была инициирована — возвращаем существующую
        var existing = paymentRepository.findByOrderId(order.getId());
        if (existing.isPresent()) {
            Payment p = existing.get();
            if (p.getStatus() == PaymentStatus.SUCCESS) {
                throw AppException.conflict("Order already paid");
            }
            // Возвращаем существующий ключ (идемпотентность)
            return new PaymentDto.PaymentInitResponse(
                    p.getId(), p.getIdempotencyKey(),
                    p.getAmount(), p.getProvider()
            );
        }

        String idempotencyKey = UUID.randomUUID().toString();

        Payment payment = Payment.create(
                order, req.getProvider(),
                order.getTotal(), idempotencyKey
        );
        paymentRepository.save(payment);

        return new PaymentDto.PaymentInitResponse(
                payment.getId(), idempotencyKey,
                payment.getAmount(), payment.getProvider()
        );
    }

    // ── Подтверждение оплаты ──────────────────────────────
    // Шаг 2: после оплаты Toss возвращает paymentKey на фронт.
    // Фронт передаёт его нам, мы подтверждаем через Toss API.

    @Transactional
    public PaymentDto.PaymentResponse confirmPayment(UUID userId,
                                                     PaymentDto.ConfirmRequest req) {
        Payment payment = paymentRepository.findByIdempotencyKey(req.getIdempotencyKey())
                .orElseThrow(() -> AppException.notFound("Payment not found"));

        if (!payment.getOrder().getUser().getId().equals(userId)) {
            throw AppException.forbidden("Not your payment");
        }

        // Уже обработан — идемпотентный ответ
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return PaymentDto.PaymentResponse.from(payment);
        }

        // Проверяем сумму — фронт не может подменить
        if (payment.getAmount().compareTo(req.getAmount()) != 0) {
            payment.fail("Amount mismatch");
            log.warn("Payment amount mismatch for order {}: expected {}, got {}",
                    payment.getOrder().getId(), payment.getAmount(), req.getAmount());
            throw AppException.badRequest("Payment amount mismatch");
        }

        // Подтверждаем через Toss API
        try {
            TossPaymentResponse tossResponse = tossClient.confirmPayment(
                    req.getProviderTxId(),
                    req.getIdempotencyKey(),
                    req.getAmount()
            );

            if (tossResponse.isSuccess()) {
                payment.succeed(tossResponse.getPaymentKey());
                payment.getOrder().confirm();
                log.info("Payment confirmed via Toss: orderId={}, paymentKey={}",
                        payment.getOrder().getId(), tossResponse.getPaymentKey());
            } else {
                payment.fail(tossResponse.getMessage());
                log.warn("Payment failed via Toss: orderId={}, reason={}",
                        payment.getOrder().getId(), tossResponse.getMessage());
                throw AppException.badRequest("Payment failed: " + tossResponse.getMessage());
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            payment.fail(e.getMessage());
            log.error("Toss API error for order {}", payment.getOrder().getId(), e);
            throw AppException.badRequest("Payment processing error");
        }

        return PaymentDto.PaymentResponse.from(payment);
    }

    // ── Webhook от Toss ───────────────────────────────────
    // Toss сам уведомляет нас об изменении статуса.
    // Используется как резервный механизм если фронт не вызвал confirm.

    @Transactional
    public void handleWebhook(PaymentDto.WebhookRequest req) {
        Payment payment = paymentRepository.findByProviderTxId(req.getProviderTxId())
                .orElseThrow(() -> {
                    log.warn("Webhook for unknown txId: {}", req.getProviderTxId());
                    return AppException.notFound("Payment not found");
                });

        // Уже финальный статус — игнорируем повторный webhook
        if (payment.getStatus() == PaymentStatus.SUCCESS
                || payment.getStatus() == PaymentStatus.REFUNDED) {
            log.info("Ignoring webhook for finalized payment: {}", req.getProviderTxId());
            return;
        }

        switch (req.getStatus().toUpperCase()) {
            case "DONE" -> {
                payment.succeed(req.getProviderTxId());
                payment.getOrder().confirm();
                log.info("Webhook: payment succeeded for order {}",
                        payment.getOrder().getId());
            }
            case "ABORTED" -> {
                payment.fail(req.getFailureReason());
                log.warn("Webhook: payment failed for order {}, reason: {}",
                        payment.getOrder().getId(), req.getFailureReason());
            }
            case "CANCELED" -> {
                payment.cancel();
                payment.getOrder().cancel();
                log.info("Webhook: payment cancelled for order {}",
                        payment.getOrder().getId());
            }
            default -> log.warn("Webhook: unknown status {}", req.getStatus());
        }
    }

    // ── Возврат ───────────────────────────────────────────

    @Transactional
    public PaymentDto.PaymentResponse refund(PaymentDto.RefundRequest req) {
        Payment payment = paymentRepository.findByOrderId(req.getOrderId())
                .orElseThrow(() -> AppException.notFound("Payment not found"));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw AppException.badRequest("Payment is not in SUCCESS status");
        }

        // Вызываем Toss API для возврата
        try {
            tossClient.cancelPayment(
                    payment.getProviderTxId(),
                    req.getReason() != null ? req.getReason() : "관리자 환불"
            );
        } catch (Exception e) {
            log.error("Toss refund error for order {}", req.getOrderId(), e);
            throw AppException.badRequest("Refund processing error");
        }

        payment.refund();
        payment.getOrder().cancel();

        log.info("Refund processed for order {}, reason: {}",
                req.getOrderId(), req.getReason());

        return PaymentDto.PaymentResponse.from(payment);
    }

    // ── Получение статуса ─────────────────────────────────

    public PaymentDto.PaymentResponse getPayment(UUID userId, UUID orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> AppException.notFound("Payment not found"));

        if (!payment.getOrder().getUser().getId().equals(userId)) {
            throw AppException.forbidden("Not your payment");
        }

        return PaymentDto.PaymentResponse.from(payment);
    }
}