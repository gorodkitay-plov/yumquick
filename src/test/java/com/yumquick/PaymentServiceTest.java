package com.yumquick.payment;

import com.yumquick.common.exception.AppException;
import com.yumquick.order.Order;
import com.yumquick.order.OrderRepository;
import com.yumquick.order.OrderStatus;
import com.yumquick.user.Role;
import com.yumquick.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService 테스트")
class PaymentServiceTest {

    @Mock PaymentRepository paymentRepository;
    @Mock OrderRepository orderRepository;
    @Mock TossPaymentClient tossClient;

    @InjectMocks PaymentService paymentService;

    @Test
    @DisplayName("존재하지 않는 주문으로 결제 초기화하면 404 에러")
    void initPayment_orderNotFound_throwsNotFound() {
        // given
        UUID userId = UUID.randomUUID();
        PaymentDto.InitRequest request = new PaymentDto.InitRequest();
        setField(request, "orderId", UUID.randomUUID());
        setField(request, "provider", PaymentProvider.TOSS);

        given(orderRepository.findByIdAndUserId(any(), eq(userId)))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> paymentService.initPayment(userId, request))
                .isInstanceOf(AppException.class);
    }

    @Test
    @DisplayName("이미 결제된 주문에 결제 초기화하면 409 에러")
    void initPayment_alreadyPaid_throwsConflict() {
        // given
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        User user = User.create("Test", "test@test.com", "pw", Role.USER);
        Order order = mock(Order.class);
        given(order.getStatus()).willReturn(OrderStatus.CONFIRMED);

        PaymentDto.InitRequest request = new PaymentDto.InitRequest();
        setField(request, "orderId", orderId);
        setField(request, "provider", PaymentProvider.TOSS);

        given(orderRepository.findByIdAndUserId(orderId, userId))
                .willReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> paymentService.initPayment(userId, request))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("not awaiting payment");
    }

    @Test
    @DisplayName("금액 불일치 시 결제 확인 실패")
    void confirmPayment_amountMismatch_throwsBadRequest() {
        // given
        UUID userId = UUID.randomUUID();
        String idempotencyKey = UUID.randomUUID().toString();

        User user = User.create("Test", "test@test.com", "pw", Role.USER);
        Order order = mock(Order.class);
        given(order.getUser()).willReturn(user);

        Payment payment = mock(Payment.class);
        given(payment.getStatus()).willReturn(PaymentStatus.PENDING);
        given(payment.getAmount()).willReturn(BigDecimal.valueOf(15000));
        given(payment.getOrder()).willReturn(order);

        given(paymentRepository.findByIdempotencyKey(idempotencyKey))
                .willReturn(Optional.of(payment));

        PaymentDto.ConfirmRequest request = new PaymentDto.ConfirmRequest();
        setField(request, "idempotencyKey", idempotencyKey);
        setField(request, "providerTxId", "toss_tx_123");
        setField(request, "amount", BigDecimal.valueOf(10000)); // другая сумма

        // when & then
        assertThatThrownBy(() -> paymentService.confirmPayment(userId, request))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Amount mismatch");
    }

    // ── Helper ────────────────────────────────────────────

    private void setField(Object obj, String fieldName, Object value) {
        try {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}