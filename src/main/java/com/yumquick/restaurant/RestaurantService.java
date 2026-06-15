package com.yumquick.restaurant;

import com.yumquick.common.exception.AppException;
import com.yumquick.user.User;
import com.yumquick.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantHoursRepository hoursRepository;
    private final UserRepository userRepository;

    // ── Public APIs ───────────────────────────────────────

    public Page<RestaurantDto.RestaurantResponse> getAll(Pageable pageable) {
        return restaurantRepository.findByActiveTrueAndOpenTrue(pageable)
                .map(RestaurantDto.RestaurantResponse::from);
    }

    public Page<RestaurantDto.RestaurantResponse> search(String keyword, Pageable pageable) {
        return restaurantRepository
                .findByActiveTrueAndNameContainingIgnoreCase(keyword, pageable)
                .map(RestaurantDto.RestaurantResponse::from);
    }

    public List<RestaurantDto.RestaurantResponse> getNearby(RestaurantDto.NearbyRequest req) {
        return restaurantRepository
                .findNearby(req.getLat(), req.getLng(), req.getRadiusKm(), req.getLimit())
                .stream()
                .map(RestaurantDto.RestaurantResponse::from)
                .toList();
    }

    public Page<RestaurantDto.RestaurantResponse> getPopular(Pageable pageable) {
        return restaurantRepository
                .findByActiveTrueAndOpenTrueOrderByRatingDescRatingCountDesc(pageable)
                .map(RestaurantDto.RestaurantResponse::from);
    }

    public RestaurantDto.RestaurantResponse getById(UUID id) {
        Restaurant restaurant = findActiveById(id);
        List<RestaurantHours> hours = hoursRepository.findByRestaurantIdOrderByDayOfWeek(id);
        return RestaurantDto.RestaurantResponse.from(restaurant, hours);
    }

    // ── Owner APIs ────────────────────────────────────────

    public List<RestaurantDto.RestaurantResponse> getMyRestaurants(UUID ownerId) {
        return restaurantRepository.findByOwnerIdAndActiveTrue(ownerId)
                .stream()
                .map(RestaurantDto.RestaurantResponse::from)
                .toList();
    }

    @Transactional
    public RestaurantDto.RestaurantResponse create(UUID ownerId,
                                                    RestaurantDto.CreateRequest req) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> AppException.notFound("User not found"));

        Restaurant restaurant = Restaurant.create(
                owner, req.getName(), req.getDescription(),
                req.getLat(), req.getLng(), req.getAddressDetail(),
                req.getDeliveryFee(), req.getMinOrder()
        );
        restaurantRepository.save(restaurant);

        if (req.getHours() != null) {
            saveHours(restaurant, req.getHours());
        }

        List<RestaurantHours> hours = hoursRepository
                .findByRestaurantIdOrderByDayOfWeek(restaurant.getId());
        return RestaurantDto.RestaurantResponse.from(restaurant, hours);
    }

    @Transactional
    public RestaurantDto.RestaurantResponse update(UUID ownerId, UUID restaurantId,
                                                    RestaurantDto.UpdateRequest req) {
        Restaurant restaurant = findOwnedBy(restaurantId, ownerId);
        restaurant.update(req.getName(), req.getDescription(), req.getAddressDetail(),
                req.getDeliveryFee(), req.getMinOrder(), req.getEstimatedDeliveryMinutes());

        List<RestaurantHours> hours = hoursRepository
                .findByRestaurantIdOrderByDayOfWeek(restaurantId);
        return RestaurantDto.RestaurantResponse.from(restaurant, hours);
    }

    @Transactional
    public void updateHours(UUID ownerId, UUID restaurantId,
                             List<RestaurantDto.HoursRequest> hoursReq) {
        Restaurant restaurant = findOwnedBy(restaurantId, ownerId);
        hoursRepository.deleteAllByRestaurantId(restaurantId);
        saveHours(restaurant, hoursReq);
    }

    @Transactional
    public void toggleOpen(UUID ownerId, UUID restaurantId, boolean open) {
        Restaurant restaurant = findOwnedBy(restaurantId, ownerId);
        if (open) restaurant.open();
        else restaurant.close();
    }

    @Transactional
    public void delete(UUID ownerId, UUID restaurantId) {
        Restaurant restaurant = findOwnedBy(restaurantId, ownerId);
        restaurant.deactivate();
    }

    // ── Helpers ───────────────────────────────────────────

    private Restaurant findActiveById(UUID id) {
        return restaurantRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> AppException.notFound("Restaurant not found"));
    }

    private Restaurant findOwnedBy(UUID restaurantId, UUID ownerId) {
        return restaurantRepository.findByIdAndOwnerId(restaurantId, ownerId)
                .orElseThrow(() -> AppException.forbidden("Not your restaurant"));
    }

    private void saveHours(Restaurant restaurant, List<RestaurantDto.HoursRequest> hoursReq) {
        List<RestaurantHours> hours = hoursReq.stream().map(h -> {
            if (h.isClosed()) {
                return RestaurantHours.createClosed(restaurant, h.getDayOfWeek());
            }
            return RestaurantHours.create(restaurant, h.getDayOfWeek(),
                    h.getOpenTime(), h.getCloseTime());
        }).toList();
        hoursRepository.saveAll(hours);
    }
}
