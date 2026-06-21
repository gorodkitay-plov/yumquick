package com.yumquick.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Order 상태 전이 테스트")
class OrderStatusTest {

    // Создаём Order через рефлексию для тестов
    private Order createOrder(OrderStatus status) {
        try {
            var constructor = Order.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Order order = constructor.newInstance();
            var statusField = Order.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(order, status);
            return order;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("PENDING → CONFIRMED 가능")
    void confirm_fromPending_success() {
        Order order = createOrder(OrderStatus.PENDING);
        order.confirm();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("CONFIRMED → PREPARING 가능")
    void startPreparing_fromConfirmed_success() {
        Order order = createOrder(OrderStatus.CONFIRMED);
        order.startPreparing();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PREPARING);
    }

    @Test
    @DisplayName("PREPARING → READY_FOR_PICKUP 가능")
    void readyForPickup_fromPreparing_success() {
        Order order = createOrder(OrderStatus.PREPARING);
        order.readyForPickup();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.READY_FOR_PICKUP);
    }

    @Test
    @DisplayName("READY_FOR_PICKUP → ON_THE_WAY 가능")
    void onTheWay_fromReadyForPickup_success() {
        Order order = createOrder(OrderStatus.READY_FOR_PICKUP);
        order.onTheWay();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ON_THE_WAY);
    }

    @Test
    @DisplayName("ON_THE_WAY → DELIVERED 가능")
    void deliver_fromOnTheWay_success() {
        Order order = createOrder(OrderStatus.ON_THE_WAY);
        order.deliver();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    @DisplayName("PENDING → CANCELLED 가능")
    void cancel_fromPending_success() {
        Order order = createOrder(OrderStatus.PENDING);
        order.cancel();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("PENDING → DELIVERED 불가능 (IllegalStateException)")
    void deliver_fromPending_throwsException() {
        Order order = createOrder(OrderStatus.PENDING);
        assertThatThrownBy(order::deliver)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("DELIVERED → CANCELLED 불가능 (IllegalStateException)")
    void cancel_fromDelivered_throwsException() {
        Order order = createOrder(OrderStatus.DELIVERED);
        assertThatThrownBy(order::cancel)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ON_THE_WAY → CANCELLED 불가능 (IllegalStateException)")
    void cancel_fromOnTheWay_throwsException() {
        Order order = createOrder(OrderStatus.ON_THE_WAY);
        assertThatThrownBy(order::cancel)
                .isInstanceOf(IllegalStateException.class);
    }
}