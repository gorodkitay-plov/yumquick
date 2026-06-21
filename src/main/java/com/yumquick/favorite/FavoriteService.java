package com.yumquick.favorite;

import com.yumquick.common.exception.AppException;
import com.yumquick.restaurant.Restaurant;
import com.yumquick.restaurant.RestaurantRepository;
import com.yumquick.user.User;
import com.yumquick.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    public Page<FavoriteDto.FavoriteResponse> getFavorites(UUID userId, Pageable pageable) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(FavoriteDto.FavoriteResponse::from);
    }

    public FavoriteDto.FavoriteStatusResponse getStatus(UUID userId, UUID restaurantId) {
        return new FavoriteDto.FavoriteStatusResponse(
                favoriteRepository.existsByUserIdAndRestaurantId(userId, restaurantId)
        );
    }

    @Transactional
    public FavoriteDto.FavoriteResponse add(UUID userId, UUID restaurantId) {
        if (favoriteRepository.existsByUserIdAndRestaurantId(userId, restaurantId)) {
            throw AppException.conflict("Already in favorites");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> AppException.notFound("User not found"));

        Restaurant restaurant = restaurantRepository.findByIdAndActiveTrue(restaurantId)
                .orElseThrow(() -> AppException.notFound("Restaurant not found"));

        Favorite favorite = Favorite.create(user, restaurant);
        favoriteRepository.save(favorite);
        return FavoriteDto.FavoriteResponse.from(favorite);
    }

    @Transactional
    public void remove(UUID userId, UUID restaurantId) {
        if (!favoriteRepository.existsByUserIdAndRestaurantId(userId, restaurantId)) {
            throw AppException.notFound("Not in favorites");
        }
        favoriteRepository.deleteByUserIdAndRestaurantId(userId, restaurantId);
    }
}
