package com.yumquick.order;

public enum OrderStatus {
    PENDING,           // Ожидание оплаты
    CONFIRMED,         // Подтверждение рестораном
    PREPARING,         // Готовится
    READY_FOR_PICKUP,  // Ожидание курьера
    ON_THE_WAY,        // В пути
    DELIVERED,         // Доставлен
    CANCELLED          // Отменён
}
