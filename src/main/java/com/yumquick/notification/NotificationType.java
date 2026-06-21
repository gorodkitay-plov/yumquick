package com.yumquick.notification;

public enum NotificationType {
    ORDER_CONFIRMED,     // Заказ подтверждён рестораном
    ORDER_PREPARING,     // Заказ готовится
    ORDER_READY,         // Заказ готов к выдаче курьеру
    COURIER_ASSIGNED,    // Курьер назначен
    COURIER_PICKED_UP,   // Курьер забрал заказ
    ORDER_DELIVERED,     // Заказ доставлен
    ORDER_CANCELLED,     // Заказ отменён
    PROMO               // Промо акция
}
