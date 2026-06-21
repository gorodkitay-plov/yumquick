package com.yumquick.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
public class TossPaymentClient {

    private final RestClient restClient;

    public TossPaymentClient(
            @Value("${toss.payments.secret-key}") String secretKey,
            @Value("${toss.payments.base-url}") String baseUrl) {

        // Toss использует Basic Auth: Base64(secretKey + ":")
        String encoded = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes());

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoded)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    // ── Подтверждение платежа ─────────────────────────────
    // Вызывается после того как пользователь оплатил на стороне Toss

    public TossPaymentResponse confirmPayment(String paymentKey,
                                              String orderId,
                                              BigDecimal amount) {
        Map<String, Object> body = Map.of(
                "paymentKey", paymentKey,
                "orderId", orderId,
                "amount", amount
        );

        log.info("Toss confirm: paymentKey={}, orderId={}, amount={}",
                paymentKey, orderId, amount);

        return restClient.post()
                .uri("/v1/payments/confirm")
                .body(body)
                .retrieve()
                .body(TossPaymentResponse.class);
    }

    // ── Отмена / возврат ──────────────────────────────────

    public TossPaymentResponse cancelPayment(String paymentKey, String reason) {
        Map<String, Object> body = Map.of("cancelReason", reason);

        log.info("Toss cancel: paymentKey={}, reason={}", paymentKey, reason);

        return restClient.post()
                .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                .body(body)
                .retrieve()
                .body(TossPaymentResponse.class);
    }

    // ── Получение статуса платежа ─────────────────────────

    public TossPaymentResponse getPayment(String paymentKey) {
        return restClient.get()
                .uri("/v1/payments/{paymentKey}", paymentKey)
                .retrieve()
                .body(TossPaymentResponse.class);
    }
}
