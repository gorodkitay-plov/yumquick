package com.yumquick.admin;

import com.yumquick.order.OrderStatus;
import com.yumquick.user.Role;
import com.yumquick.user.User;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class AdminDto {

    // ── User management ───────────────────────────────────

    @Getter
    public static class ChangeRoleRequest {
        private Role role;
    }

    public record UserSummary(
            UUID id,
            String name,
            String email,
            String phone,
            Role role,
            boolean active,
            LocalDateTime createdAt
    ) {
        public static UserSummary from(User user) {
            return new UserSummary(
                    user.getId(), user.getName(), user.getEmail(),
                    user.getPhone(), user.getRole(),
                    user.isActive(), user.getCreatedAt()
            );
        }
    }

    // ── Analytics ─────────────────────────────────────────

    public record DashboardStats(
            long totalUsers,
            long totalRestaurants,
            long totalOrders,
            long pendingOrders,
            BigDecimal totalRevenue
    ) {}

    // ── Restaurant moderation ──────────────────────────────

    @Getter
    public static class ToggleRestaurantRequest {
        private boolean active;
    }
}
