package com.yumquick.review;

import com.yumquick.common.exception.AppException;
import com.yumquick.order.Order;
import com.yumquick.order.OrderRepository;
import com.yumquick.order.OrderStatus;
import com.yumquick.restaurant.Restaurant;
import com.yumquick.restaurant.RestaurantRepository;
import com.yumquick.user.User;
import com.yumquick.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    // ── Создать отзыв ─────────────────────────────────────

    @Transactional
    public ReviewDto.ReviewResponse create(UUID userId, ReviewDto.CreateRequest req) {
        // Проверяем что заказ существует и принадлежит пользователю
        Order order = orderRepository.findByIdAndUserId(req.getOrderId(), userId)
                .orElseThrow(() -> AppException.notFound("Order not found"));

        // Отзыв можно оставить только на доставленный заказ
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw AppException.badRequest("Can only review delivered orders");
        }

        // Один заказ — один отзыв
        if (reviewRepository.existsByOrderId(req.getOrderId())) {
            throw AppException.conflict("Review already exists for this order");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> AppException.notFound("User not found"));

        Restaurant restaurant = order.getRestaurant();

        Review review = Review.create(
                order, user, restaurant,
                req.getRestaurantRating(),
                req.getCourierRating(),
                req.getComment()
        );
        reviewRepository.save(review);

        // Пересчитываем рейтинг ресторана
        recalculateRestaurantRating(restaurant);

        return ReviewDto.ReviewResponse.from(review);
    }

    // ── Получить отзывы ресторана ─────────────────────────

    public Page<ReviewDto.ReviewResponse> getRestaurantReviews(UUID restaurantId,
                                                                Pageable pageable) {
        return reviewRepository
                .findByRestaurantIdAndHiddenFalseOrderByCreatedAtDesc(restaurantId, pageable)
                .map(ReviewDto.ReviewResponse::from);
    }

    // ── Мои отзывы ───────────────────────────────────────

    public Page<ReviewDto.ReviewResponse> getMyReviews(UUID userId, Pageable pageable) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(ReviewDto.ReviewResponse::from);
    }

    // ── Admin: модерация ──────────────────────────────────

    public Page<ReviewDto.ReviewResponse> getAllReviews(Pageable pageable) {
        return reviewRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(ReviewDto.ReviewResponse::from);
    }

    @Transactional
    public void hideReview(UUID reviewId) {
        Review review = findById(reviewId);
        review.hide();
        // Пересчитываем рейтинг после скрытия
        recalculateRestaurantRating(review.getRestaurant());
        log.info("Review hidden by admin: reviewId={}", reviewId);
    }

    @Transactional
    public void showReview(UUID reviewId) {
        Review review = findById(reviewId);
        review.show();
        recalculateRestaurantRating(review.getRestaurant());
    }

    // ── Helpers ───────────────────────────────────────────

    private Review findById(UUID reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> AppException.notFound("Review not found"));
    }

    // Пересчёт среднего рейтинга ресторана после каждого отзыва
    private void recalculateRestaurantRating(Restaurant restaurant) {
        Double avg = reviewRepository.averageRatingByRestaurantId(restaurant.getId());
        long count = reviewRepository.countByRestaurantIdAndHiddenFalse(restaurant.getId());

        BigDecimal newRating = avg == null ? BigDecimal.ZERO :
                BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP);

        restaurant.updateRating(newRating, (int) count);
        restaurantRepository.save(restaurant);

        log.debug("Restaurant rating recalculated: restaurantId={}, rating={}, count={}",
                restaurant.getId(), newRating, count);
    }
}
