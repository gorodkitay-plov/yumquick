package com.yumquick.admin;

import com.yumquick.common.exception.AppException;
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
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;

    // ── Users ─────────────────────────────────────────────

    public Page<AdminDto.UserSummary> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(AdminDto.UserSummary::from);
    }

    public AdminDto.UserSummary getUser(UUID userId) {
        return AdminDto.UserSummary.from(findUser(userId));
    }

    @Transactional
    public AdminDto.UserSummary changeRole(UUID userId, AdminDto.ChangeRoleRequest req) {
        User user = findUser(userId);
        // Роль хранится в entity — добавляем метод через сеттер
        // (В User нет setRole — добавляем changeRole)
        user.changeRole(req.getRole());
        log.info("User role changed: userId={}, newRole={}", userId, req.getRole());
        return AdminDto.UserSummary.from(user);
    }

    @Transactional
    public void banUser(UUID userId) {
        User user = findUser(userId);
        user.deactivate();
        log.info("User banned by admin: userId={}", userId);
    }

    @Transactional
    public void unbanUser(UUID userId) {
        User user = findUser(userId);
        user.activate();
        log.info("User unbanned by admin: userId={}", userId);
    }

    // ── Restaurants ───────────────────────────────────────

    public Page<AdminDto.UserSummary> getRestaurants(Pageable pageable) {
        // Возвращаем через существующий RestaurantRepository
        // Детали ресторана отдаём через RestaurantController
        return userRepository.findAll(pageable).map(AdminDto.UserSummary::from);
    }

    @Transactional
    public void toggleRestaurantActive(UUID restaurantId, boolean active) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> AppException.notFound("Restaurant not found"));
        if (active) restaurant.activate();
        else restaurant.deactivate();
        log.info("Restaurant active={} by admin: restaurantId={}", active, restaurantId);
    }

    // ── Dashboard stats ───────────────────────────────────

    public AdminDto.DashboardStats getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalRestaurants = restaurantRepository.count();
        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);

        // Общая выручка — сумма всех доставленных заказов
        BigDecimal totalRevenue = orderRepository.sumTotalByStatus(OrderStatus.DELIVERED);

        return new AdminDto.DashboardStats(
                totalUsers,
                totalRestaurants,
                totalOrders,
                pendingOrders,
                totalRevenue != null ? totalRevenue : BigDecimal.ZERO
        );
    }

    // ── Helpers ───────────────────────────────────────────

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> AppException.notFound("User not found"));
    }
}
