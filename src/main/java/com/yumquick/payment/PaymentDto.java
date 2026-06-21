package com.yumquick.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentDto {

    // ── Requests ──────────────────────────────────────────

    @Getter
    public static class InitRequest {
        @NotNull
        private UUID orderId;

        @NotNull
        private PaymentProvider provider;
    }

    @Getter
    public static class ConfirmRequest {
        // ID транзакции от платёжного провайдера (приходит с фронта после оплаты)
        @NotBlank
        private String providerTxId;

        @NotBlank
        private String idempotencyKey;

        @NotNull
        private BigDecimal amount; // проверяем что совпадает с суммой заказа
    }

    // Webhook от Toss/Kakao/Naver
    @Getter
    public static class WebhookRequest {
        private String providerTxId;
        private String status;       // SUCCESS / FAIL / CANCEL
        private BigDecimal amount;
        private String failureReason;
    }

    @Getter
    public static class RefundRequest {
        @NotNull
        private UUID orderId;

        private String reason;
    }

    // ── Responses ─────────────────────────────────────────

    public record PaymentInitResponse(
            UUID paymentId,
            String idempotencyKey,
            BigDecimal amount,
            PaymentProvider provider
    ) {}

    public record PaymentResponse(
            UUID id,
            UUID orderId,
            PaymentProvider provider,
            String providerTxId,
            PaymentStatus status,
            BigDecimal amount,
            String failureReason,
            LocalDateTime paidAt,
            LocalDateTime refundedAt
    ) {
        public static PaymentResponse from(Payment payment) {
            return new PaymentResponse(
                    payment.getId(),
                    payment.getOrder().getId(),
                    payment.getProvider(),
                    payment.getProviderTxId(),
                    payment.getStatus(),
                    payment.getAmount(),
                    payment.getFailureReason(),
                    payment.getPaidAt(),
                    payment.getRefundedAt()
            );
        }
    }
}
