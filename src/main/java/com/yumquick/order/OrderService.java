package com.yumquick.order;

import com.yumquick.cart.Cart;
import com.yumquick.cart.CartItem;
import com.yumquick.cart.CartService;
import com.yumquick.common.exception.AppException;
import com.yumquick.menu.MenuItem;
import com.yumquick.menu.MenuItemRepository;
import com.yumquick.restaurant.Restaurant;
import com.yumquick.restaurant.RestaurantRepository;
import com.yumquick.user.User;
import com.yumquick.user.UserAddress;
import com.yumquick.user.UserAddressRepository;
import com.yumquick.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final UserAddressRepository addressRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final CartService cartService;

    // ── Checkout ──────────────────────────────────────────

    @Transactional
    public OrderDto.OrderResponse checkout(UUID userId, OrderDto.CheckoutRequest req) {
        // 1. Загрузка и валидация корзины
        Cart cart = cartService.loadCart(userId);
        if (cart.isEmpty()) {
            throw AppException.badRequest("Cart is empty");
        }

        // 2. Загрузка пользователя, адреса, ресторана
        User user = userRepository.findById(userId)
                .orElseThrow(() -> AppException.notFound("User not found"));

        UserAddress address = addressRepository
                .findByIdAndUserId(req.getAddressId(), userId)
                .orElseThrow(() -> AppException.notFound("Address not found"));

        Restaurant restaurant = restaurantRepository
                .findByIdAndActiveTrue(cart.getRestaurantId())
                .orElseThrow(() -> AppException.notFound("Restaurant not found"));

        if (!restaurant.isOpen()) {
            throw AppException.badRequest("Restaurant is currently closed");
        }

        // 3. Пересчёт цен на сервере (фронту не доверяем)
        BigDecimal subtotal = recalculateSubtotal(cart);

        if (subtotal.compareTo(restaurant.getMinOrder()) < 0) {
            throw AppException.badRequest(
                    "Minimum order amount is " + restaurant.getMinOrder());
        }

        BigDecimal deliveryFee = restaurant.getDeliveryFee();
        BigDecimal discount = BigDecimal.ZERO; // TODO: купоны Phase 4

        // 4. Создание заказа
        Order order = Order.create(user, restaurant, address,
                subtotal, deliveryFee, discount, req.getNotes());
        orderRepository.save(order);

        // 5. Создание позиций заказа (снапшот)
        for (CartItem cartItem : cart.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(cartItem.getMenuItemId())
                    .orElseThrow(() -> AppException.badRequest(
                            "Menu item no longer available: " + cartItem.getTitle()));

            if (!menuItem.isAvailable()) {
                throw AppException.badRequest(
                        "Menu item is no longer available: " + menuItem.getTitle());
            }

            OrderItem orderItem = OrderItem.create(
                    order,
                    menuItem.getId(),
                    menuItem.getTitle(),       // снапшот
                    menuItem.getPrice(),       // снапшот
                    cartItem.getQuantity()
            );

            if (cartItem.getOptions() != null) {
                cartItem.getOptions().forEach(opt ->
                        orderItem.addOption(opt.getName(), opt.getExtraPrice()));
            }

            order.getItems().add(orderItem);
        }

        // 6. Очистка корзины
        cartService.clearCart(userId);

        return OrderDto.OrderResponse.from(order);
    }

    // ── User APIs ─────────────────────────────────────────

    public Page<OrderDto.OrderResponse> getMyOrders(UUID userId, Pageable pageable) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(OrderDto.OrderResponse::from);
    }

    public OrderDto.OrderResponse getMyOrder(UUID userId, UUID orderId) {
        return OrderDto.OrderResponse.from(findUserOrder(userId, orderId));
    }

    @Transactional
    public void cancelOrder(UUID userId, UUID orderId) {
        Order order = findUserOrder(userId, orderId);
        order.cancel();
    }

    // ── Restaurant APIs ───────────────────────────────────

    public Page<OrderDto.OrderResponse> getRestaurantOrders(UUID restaurantId,
                                                             Pageable pageable) {
        return orderRepository
                .findByRestaurantIdOrderByCreatedAtDesc(restaurantId, pageable)
                .map(OrderDto.OrderResponse::from);
    }

    public Page<OrderDto.OrderResponse> getActiveOrders(UUID restaurantId, Pageable pageable) {
        return orderRepository.findActiveByRestaurantId(restaurantId, pageable)
                .map(OrderDto.OrderResponse::from);
    }

    @Transactional
    public OrderDto.OrderResponse updateStatus(UUID restaurantId, UUID orderId,
                                               OrderDto.StatusUpdateRequest req) {
        Order order = orderRepository.findByIdAndRestaurantId(orderId, restaurantId)
                .orElseThrow(() -> AppException.notFound("Order not found"));

        switch (req.getStatus()) {
            case CONFIRMED -> order.confirm();
            case PREPARING -> order.startPreparing();
            case READY_FOR_PICKUP -> order.readyForPickup();
            default -> throw AppException.badRequest("Invalid status: " + req.getStatus());
        }

        return OrderDto.OrderResponse.from(order);
    }

    // ── Helpers ───────────────────────────────────────────

    private Order findUserOrder(UUID userId, UUID orderId) {
        return orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> AppException.notFound("Order not found"));
    }

    // Пересчёт цен на сервере — unitPrice из корзины не доверяем, берём из БД
    private BigDecimal recalculateSubtotal(Cart cart) {
        return cart.getItems().stream().map(cartItem -> {
            MenuItem menuItem = menuItemRepository.findById(cartItem.getMenuItemId())
                    .orElseThrow(() -> AppException.badRequest(
                            "Menu item not found: " + cartItem.getMenuItemId()));

            BigDecimal optionsPrice = cartItem.getOptions() == null ? BigDecimal.ZERO :
                    cartItem.getOptions().stream()
                            .map(CartItem.CartItemOption::getExtraPrice)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

            return menuItem.getPrice()
                    .add(optionsPrice)
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        }).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
