package com.yumquick.order;

import com.yumquick.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Orders")
public class OrderController {

    private final OrderService orderService;

    // ── User ──────────────────────────────────────────────

    @PostMapping("/api/orders/checkout")
    @Operation(summary = "Place an order from cart")
    public ResponseEntity<ApiResponse<OrderDto.OrderResponse>> checkout(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody OrderDto.CheckoutRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(orderService.checkout(userId, request)));
    }

    @GetMapping("/api/orders")
    @Operation(summary = "Get my order history")
    public ResponseEntity<ApiResponse<Page<OrderDto.OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal UUID userId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getMyOrders(userId, pageable)));
    }

    @GetMapping("/api/orders/{orderId}")
    @Operation(summary = "Get order details")
    public ResponseEntity<ApiResponse<OrderDto.OrderResponse>> getMyOrder(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getMyOrder(userId, orderId)));
    }

    @PatchMapping("/api/orders/{orderId}/cancel")
    @Operation(summary = "Cancel an order")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID orderId) {
        orderService.cancelOrder(userId, orderId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // ── Restaurant owner ──────────────────────────────────

    @GetMapping("/api/owner/restaurants/{restaurantId}/orders")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Get all restaurant orders")
    public ResponseEntity<ApiResponse<Page<OrderDto.OrderResponse>>> getRestaurantOrders(
            @PathVariable UUID restaurantId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                orderService.getRestaurantOrders(restaurantId, pageable)));
    }

    @GetMapping("/api/owner/restaurants/{restaurantId}/orders/active")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Get active orders (incoming)")
    public ResponseEntity<ApiResponse<Page<OrderDto.OrderResponse>>> getActiveOrders(
            @PathVariable UUID restaurantId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                orderService.getActiveOrders(restaurantId, pageable)));
    }

    @PatchMapping("/api/owner/restaurants/{restaurantId}/orders/{orderId}/status")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Update order status")
    public ResponseEntity<ApiResponse<OrderDto.OrderResponse>> updateStatus(
            @PathVariable UUID restaurantId,
            @PathVariable UUID orderId,
            @RequestBody OrderDto.StatusUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                orderService.updateStatus(restaurantId, orderId, request)));
    }
}
