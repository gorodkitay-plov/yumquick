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

    // ── Инициализация оплаты ──────────────────────────────
    // Создаём запись Payment до того как пользователь платит.
    // Генерируем idempotencyKey — фронт передаёт его провайдеру.

    @Transactional
    public PaymentDto.PaymentInitResponse initPayment(UUID userId,
                                                       PaymentDto.InitRequest req) {
        Order order = orderRepository.findByIdAndUserId(req.getOrderId(), userId)
                .orElseThrow(() -> AppException.notFound("Order not found"));

        // Заказ должен быть в статусе PENDING
        if (order.getStatus() != OrderStatus.PENDING) {
            throw AppException.badRequest("Order is not awaiting payment");
        }

        // Если оплата уже была инициирована — возвращаем существующую
        paymentRepository.findByOrderId(order.getId()).ifPresent(existing -> {
            if (existing.getStatus() == PaymentStatus.SUCCESS) {
                throw AppException.conflict("Order already paid");
            }
        });

        String idempotencyKey = UUID.randomUUID().toString();

        Payment payment = Payment.create(
                order,
                req.getProvider(),
                order.getTotal(),
                idempotencyKey
        );
        paymentRepository.save(payment);

        return new PaymentDto.PaymentInitResponse(
                payment.getId(),
                idempotencyKey,
                payment.getAmount(),
                payment.getProvider()
        );
    }

    // ── Подтверждение оплаты ──────────────────────────────
    // Вызывается после того как пользователь оплатил на стороне провайдера.
    // Проверяем idempotencyKey и сумму — не доверяем фронту.

    @Transactional
    public PaymentDto.PaymentResponse confirmPayment(UUID userId,
                                                      PaymentDto.ConfirmRequest req) {
        // Ищем по idempotencyKey — защита от двойного подтверждения
        Payment payment = paymentRepository.findByIdempotencyKey(req.getIdempotencyKey())
                .orElseThrow(() -> AppException.notFound("Payment not found"));

        // Проверяем что это заказ текущего пользователя
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

        // Подтверждаем оплату
        payment.succeed(req.getProviderTxId());

        // Переводим заказ в CONFIRMED
        payment.getOrder().confirm();

        log.info("Payment confirmed: orderId={}, provider={}, txId={}",
                payment.getOrder().getId(), payment.getProvider(), req.getProviderTxId());

        return PaymentDto.PaymentResponse.from(payment);
    }

    // ── Webhook от провайдера ─────────────────────────────
    // Провайдер сам уведомляет нас об изменении статуса платежа.
    // Важно: проверять подпись webhook (реализуется отдельно для каждого провайдера).

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
            log.info("Ignoring webhook for already finalized payment: {}", req.getProviderTxId());
            return;
        }

        switch (req.getStatus().toUpperCase()) {
            case "SUCCESS" -> {
                payment.succeed(req.getProviderTxId());
                payment.getOrder().confirm();
                log.info("Webhook: payment succeeded for order {}",
                        payment.getOrder().getId());
            }
            case "FAIL" -> {
                payment.fail(req.getFailureReason());
                log.warn("Webhook: payment failed for order {}, reason: {}",
                        payment.getOrder().getId(), req.getFailureReason());
            }
            case "CANCEL" -> {
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

        // TODO: вызов API провайдера для возврата средств

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
