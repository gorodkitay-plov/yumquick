package com.yumquick.delivery;

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

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Delivery")
public class DeliveryController {

    private final DeliveryService deliveryService;

    // ── Admin: назначить курьера ──────────────────────────

    @PostMapping("/api/admin/deliveries/assign")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign courier to order")
    public ResponseEntity<ApiResponse<DeliveryDto.DeliveryResponse>> assign(
            @Valid @RequestBody DeliveryDto.AssignRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(deliveryService.assign(request)));
    }

    // ── Курьер: действия ─────────────────────────────────

    @PostMapping("/api/courier/orders/{orderId}/pickup")
    @PreAuthorize("hasRole('DELIVERY')")
    @Operation(summary = "Courier: picked up the order")
    public ResponseEntity<ApiResponse<DeliveryDto.DeliveryResponse>> pickup(
            @AuthenticationPrincipal UUID courierId,
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(ApiResponse.ok(
                deliveryService.pickup(courierId, orderId)));
    }

    @PatchMapping("/api/courier/orders/{orderId}/location")
    @PreAuthorize("hasRole('DELIVERY')")
    @Operation(summary = "Courier: update current location")
    public ResponseEntity<ApiResponse<DeliveryDto.LocationResponse>> updateLocation(
            @AuthenticationPrincipal UUID courierId,
            @PathVariable UUID orderId,
            @Valid @RequestBody DeliveryDto.LocationUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                deliveryService.updateLocation(courierId, orderId, request)));
    }

    @PostMapping("/api/courier/orders/{orderId}/complete")
    @PreAuthorize("hasRole('DELIVERY')")
    @Operation(summary = "Courier: mark order as delivered")
    public ResponseEntity<ApiResponse<DeliveryDto.DeliveryResponse>> complete(
            @AuthenticationPrincipal UUID courierId,
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(ApiResponse.ok(
                deliveryService.complete(courierId, orderId)));
    }

    @GetMapping("/api/courier/deliveries")
    @PreAuthorize("hasRole('DELIVERY')")
    @Operation(summary = "Courier: get active deliveries")
    public ResponseEntity<ApiResponse<List<DeliveryDto.DeliveryResponse>>> getActive(
            @AuthenticationPrincipal UUID courierId) {
        return ResponseEntity.ok(ApiResponse.ok(
                deliveryService.getActiveCourierDeliveries(courierId)));
    }

    @GetMapping("/api/courier/deliveries/history")
    @PreAuthorize("hasRole('DELIVERY')")
    @Operation(summary = "Courier: delivery history")
    public ResponseEntity<ApiResponse<Page<DeliveryDto.DeliveryResponse>>> getHistory(
            @AuthenticationPrincipal UUID courierId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                deliveryService.getCourierHistory(courierId, pageable)));
    }

    // ── User: отслеживание ────────────────────────────────

    @GetMapping("/api/orders/{orderId}/delivery")
    @Operation(summary = "Get delivery status for order")
    public ResponseEntity<ApiResponse<DeliveryDto.DeliveryResponse>> getDelivery(
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(ApiResponse.ok(
                deliveryService.getByOrderId(orderId)));
    }

    @GetMapping("/api/orders/{orderId}/delivery/location")
    @Operation(summary = "Get courier live location")
    public ResponseEntity<ApiResponse<DeliveryDto.LocationResponse>> getLocation(
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(ApiResponse.ok(
                deliveryService.getLocation(orderId)));
    }
}
