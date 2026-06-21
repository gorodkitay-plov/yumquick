package com.yumquick.favorite;

import com.yumquick.restaurant.RestaurantDto;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

public class FavoriteDto {

    public record FavoriteResponse(
            UUID id,
            RestaurantDto.RestaurantResponse restaurant,
            LocalDateTime createdAt
    ) {
        public static FavoriteResponse from(Favorite favorite) {
            return new FavoriteResponse(
                    favorite.getId(),
                    RestaurantDto.RestaurantResponse.from(favorite.getRestaurant()),
                    favorite.getCreatedAt()
            );
        }
    }

    public record FavoriteStatusResponse(boolean isFavorite) {}
}
