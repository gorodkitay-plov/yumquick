package com.yumquick.favorite;

import com.yumquick.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@Tag(name = "Favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    @Operation(summary = "Get my favorite restaurants")
    public ResponseEntity<ApiResponse<Page<FavoriteDto.FavoriteResponse>>> getFavorites(
            @AuthenticationPrincipal UUID userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                favoriteService.getFavorites(userId, pageable)));
    }

    @GetMapping("/{restaurantId}/status")
    @Operation(summary = "Check if restaurant is in favorites")
    public ResponseEntity<ApiResponse<FavoriteDto.FavoriteStatusResponse>> getStatus(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID restaurantId) {
        return ResponseEntity.ok(ApiResponse.ok(
                favoriteService.getStatus(userId, restaurantId)));
    }

    @PostMapping("/{restaurantId}")
    @Operation(summary = "Add restaurant to favorites")
    public ResponseEntity<ApiResponse<FavoriteDto.FavoriteResponse>> add(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID restaurantId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(favoriteService.add(userId, restaurantId)));
    }

    @DeleteMapping("/{restaurantId}")
    @Operation(summary = "Remove restaurant from favorites")
    public ResponseEntity<ApiResponse<Void>> remove(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID restaurantId) {
        favoriteService.remove(userId, restaurantId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
