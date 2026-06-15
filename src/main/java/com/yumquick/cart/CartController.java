package com.yumquick.cart;

import com.yumquick.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get current cart")
    public ResponseEntity<ApiResponse<CartDto.CartResponse>> getCart(
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(cartService.getCart(userId)));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<ApiResponse<CartDto.CartResponse>> addItem(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody CartDto.AddItemRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(cartService.addItem(userId, request)));
    }

    @PatchMapping("/items")
    @Operation(summary = "Update item quantity (0 = remove)")
    public ResponseEntity<ApiResponse<CartDto.CartResponse>> updateQuantity(
            @AuthenticationPrincipal UUID userId,
            @RequestBody CartDto.UpdateQuantityRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(cartService.updateQuantity(userId, request)));
    }

    @DeleteMapping
    @Operation(summary = "Clear cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @AuthenticationPrincipal UUID userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
