package com.yumquick.payment;

import com.yumquick.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments")
public class PaymentController {

    private final PaymentService paymentService;

    // Шаг 1: инициализировать оплату (получить idempotencyKey)
    @PostMapping("/init")
    @Operation(summary = "Initialize payment")
    public ResponseEntity<ApiResponse<PaymentDto.PaymentInitResponse>> init(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody PaymentDto.InitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(paymentService.initPayment(userId, request)));
    }

    // Шаг 2: подтвердить оплату после успеха на стороне провайдера
    @PostMapping("/confirm")
    @Operation(summary = "Confirm payment after provider success")
    public ResponseEntity<ApiResponse<PaymentDto.PaymentResponse>> confirm(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody PaymentDto.ConfirmRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                paymentService.confirmPayment(userId, request)));
    }

    // Статус оплаты по orderId
    @GetMapping("/orders/{orderId}")
    @Operation(summary = "Get payment status by orderId")
    public ResponseEntity<ApiResponse<PaymentDto.PaymentResponse>> getPayment(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(ApiResponse.ok(
                paymentService.getPayment(userId, orderId)));
    }

    // Webhook — вызывается платёжным провайдером, не пользователем
    // Не требует JWT — провайдер не авторизован в нашей системе
    // Безопасность обеспечивается проверкой подписи внутри сервиса
    @PostMapping("/webhook")
    @Operation(summary = "Payment provider webhook")
    public ResponseEntity<Void> webhook(
            @RequestBody PaymentDto.WebhookRequest request) {
        paymentService.handleWebhook(request);
        return ResponseEntity.ok().build();
    }

    // Возврат — только для Admin
    @PostMapping("/refund")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Refund payment (Admin only)")
    public ResponseEntity<ApiResponse<PaymentDto.PaymentResponse>> refund(
            @Valid @RequestBody PaymentDto.RefundRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.refund(request)));
    }
}
