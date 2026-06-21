package com.yumquick.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// @JsonIgnoreProperties — игнорируем неизвестные поля от Toss
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TossPaymentResponse {

    // Уникальный ключ платежа от Toss
    private String paymentKey;

    // ID заказа который мы передали при инициализации
    private String orderId;

    // Статус платежа: DONE / CANCELED / ABORTED / PARTIAL_CANCELED
    private String status;

    // Сумма платежа
    private BigDecimal totalAmount;

    // Метод оплаты: 카드 / 가상계좌 / 간편결제 и т.д.
    private String method;

    // Имя покупателя
    private String customerName;

    // Время подтверждения оплаты
    private String approvedAt;

    // Информация об ошибке (если статус ABORTED)
    private String code;
    private String message;

    public boolean isSuccess() {
        return "DONE".equals(status);
    }

    public boolean isFailed() {
        return "ABORTED".equals(status);
    }

    public boolean isCancelled() {
        return "CANCELED".equals(status);
    }
}
