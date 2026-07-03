package com.yumquick.restaurant;

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
@Tag(name = "Restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    // ── Public ────────────────────────────────────────────

    @GetMapping("/api/restaurants")
    public ResponseEntity<ApiResponse<Page<RestaurantDto.RestaurantResponse>>> getAll(
            @RequestParam(required = false) RestaurantCategory category,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(restaurantService.getAll(category, pageable)));
    }

    @GetMapping("/api/restaurants/search")
    @Operation(summary = "Search restaurants by name")
    public ResponseEntity<ApiResponse<Page<RestaurantDto.RestaurantResponse>>> search(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(restaurantService.search(keyword, pageable)));
    }

    @GetMapping("/api/restaurants/nearby")
    @Operation(summary = "Get nearby restaurants")
    public ResponseEntity<ApiResponse<List<RestaurantDto.RestaurantResponse>>> getNearby(
            @Valid RestaurantDto.NearbyRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(restaurantService.getNearby(request)));
    }

    @GetMapping("/api/restaurants/popular")
    @Operation(summary = "Get popular restaurants")
    public ResponseEntity<ApiResponse<Page<RestaurantDto.RestaurantResponse>>> getPopular(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(restaurantService.getPopular(pageable)));
    }

    @GetMapping("/api/restaurants/{id}")
    @Operation(summary = "Get restaurant details")
    public ResponseEntity<ApiResponse<RestaurantDto.RestaurantResponse>> getById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(restaurantService.getById(id)));
    }

    // ── Owner ─────────────────────────────────────────────

    @GetMapping("/api/owner/restaurants")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Get my restaurants")
    public ResponseEntity<ApiResponse<List<RestaurantDto.RestaurantResponse>>> getMyRestaurants(
            @AuthenticationPrincipal UUID ownerId) {
        return ResponseEntity.ok(ApiResponse.ok(restaurantService.getMyRestaurants(ownerId)));
    }

    @PostMapping("/api/owner/restaurants")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Create a restaurant")
    public ResponseEntity<ApiResponse<RestaurantDto.RestaurantResponse>> create(
            @AuthenticationPrincipal UUID ownerId,
            @Valid @RequestBody RestaurantDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(restaurantService.create(ownerId, request)));
    }

    @PatchMapping("/api/owner/restaurants/{id}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Update restaurant info")
    public ResponseEntity<ApiResponse<RestaurantDto.RestaurantResponse>> update(
            @AuthenticationPrincipal UUID ownerId,
            @PathVariable UUID id,
            @RequestBody RestaurantDto.UpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(restaurantService.update(ownerId, id, request)));
    }

    @PutMapping("/api/owner/restaurants/{id}/hours")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Update operating hours")
    public ResponseEntity<ApiResponse<Void>> updateHours(
            @AuthenticationPrincipal UUID ownerId,
            @PathVariable UUID id,
            @RequestBody List<RestaurantDto.HoursRequest> hours) {
        restaurantService.updateHours(ownerId, id, hours);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PatchMapping("/api/owner/restaurants/{id}/open")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Toggle open/closed status")
    public ResponseEntity<ApiResponse<Void>> toggleOpen(
            @AuthenticationPrincipal UUID ownerId,
            @PathVariable UUID id,
            @RequestParam boolean open) {
        restaurantService.toggleOpen(ownerId, id, open);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/api/owner/restaurants/{id}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Delete restaurant")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UUID ownerId,
            @PathVariable UUID id) {
        restaurantService.delete(ownerId, id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
