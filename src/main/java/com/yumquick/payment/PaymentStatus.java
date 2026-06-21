package com.yumquick.payment;

public enum PaymentStatus {
    PENDING,    // Ожидание оплаты
    SUCCESS,    // Оплата прошла
    FAILED,     // Ошибка оплаты
    CANCELLED,  // Отменена
    REFUNDED    // Возврат
}
