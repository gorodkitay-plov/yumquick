package com.yumquick.menu;

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

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Menu")
public class MenuController {

    private final MenuService menuService;

    // ── Public ────────────────────────────────────────────

    @GetMapping("/api/restaurants/{restaurantId}/menu")
    @Operation(summary = "Get full menu of a restaurant")
    public ResponseEntity<ApiResponse<List<MenuDto.CategoryResponse>>> getMenu(
            @PathVariable UUID restaurantId) {
        return ResponseEntity.ok(ApiResponse.ok(menuService.getMenu(restaurantId)));
    }

    @GetMapping("/api/menu/items/{itemId}")
    @Operation(summary = "Get menu item details")
    public ResponseEntity<ApiResponse<MenuDto.ItemResponse>> getItem(
            @PathVariable UUID itemId) {
        return ResponseEntity.ok(ApiResponse.ok(menuService.getItem(itemId)));
    }

    // ── Owner: categories ─────────────────────────────────

    @PostMapping("/api/owner/restaurants/{restaurantId}/categories")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Create menu category")
    public ResponseEntity<ApiResponse<MenuDto.CategoryResponse>> createCategory(
            @AuthenticationPrincipal UUID ownerId,
            @PathVariable UUID restaurantId,
            @Valid @RequestBody MenuDto.CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(menuService.createCategory(ownerId, restaurantId, request)));
    }

    @PatchMapping("/api/owner/restaurants/{restaurantId}/categories/{categoryId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Update menu category")
    public ResponseEntity<ApiResponse<MenuDto.CategoryResponse>> updateCategory(
            @AuthenticationPrincipal UUID ownerId,
            @PathVariable UUID restaurantId,
            @PathVariable UUID categoryId,
            @RequestBody MenuDto.CategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                menuService.updateCategory(ownerId, restaurantId, categoryId, request)));
    }

    @DeleteMapping("/api/owner/restaurants/{restaurantId}/categories/{categoryId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Delete menu category")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @AuthenticationPrincipal UUID ownerId,
            @PathVariable UUID restaurantId,
            @PathVariable UUID categoryId) {
        menuService.deleteCategory(ownerId, restaurantId, categoryId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // ── Owner: items ──────────────────────────────────────

    @PostMapping("/api/owner/restaurants/{restaurantId}/items")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Create menu item")
    public ResponseEntity<ApiResponse<MenuDto.ItemResponse>> createItem(
            @AuthenticationPrincipal UUID ownerId,
            @PathVariable UUID restaurantId,
            @Valid @RequestBody MenuDto.ItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(menuService.createItem(ownerId, restaurantId, request)));
    }

    @PatchMapping("/api/owner/restaurants/{restaurantId}/items/{itemId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Update menu item")
    public ResponseEntity<ApiResponse<MenuDto.ItemResponse>> updateItem(
            @AuthenticationPrincipal UUID ownerId,
            @PathVariable UUID restaurantId,
            @PathVariable UUID itemId,
            @RequestBody MenuDto.ItemUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                menuService.updateItem(ownerId, restaurantId, itemId, request)));
    }

    @PatchMapping("/api/owner/restaurants/{restaurantId}/items/{itemId}/available")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Toggle item availability")
    public ResponseEntity<ApiResponse<Void>> toggleAvailable(
            @AuthenticationPrincipal UUID ownerId,
            @PathVariable UUID restaurantId,
            @PathVariable UUID itemId,
            @RequestParam boolean available) {
        menuService.toggleAvailable(ownerId, restaurantId, itemId, available);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/api/owner/restaurants/{restaurantId}/items/{itemId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Delete menu item")
    public ResponseEntity<ApiResponse<Void>> deleteItem(
            @AuthenticationPrincipal UUID ownerId,
            @PathVariable UUID restaurantId,
            @PathVariable UUID itemId) {
        menuService.deleteItem(ownerId, restaurantId, itemId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
