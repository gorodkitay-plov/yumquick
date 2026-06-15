package com.yumquick.cart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yumquick.common.exception.AppException;
import com.yumquick.menu.MenuItem;
import com.yumquick.menu.MenuItemRepository;
import com.yumquick.menu.MenuOption;
import com.yumquick.menu.OptionGroupRepository;
import com.yumquick.restaurant.Restaurant;
import com.yumquick.restaurant.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private static final String CART_PREFIX = "cart:";
    private static final Duration CART_TTL = Duration.ofDays(7);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    // ── Get ───────────────────────────────────────────────

    public CartDto.CartResponse getCart(UUID userId) {
        Cart cart = loadCart(userId);
        return CartDto.CartResponse.from(cart);
    }

    // ── Add item ──────────────────────────────────────────

    public CartDto.CartResponse addItem(UUID userId, CartDto.AddItemRequest req) {
        MenuItem menuItem = menuItemRepository.findById(req.getMenuItemId())
                .orElseThrow(() -> AppException.notFound("Menu item not found"));

        if (!menuItem.isAvailable()) {
            throw AppException.badRequest("Menu item is not available");
        }

        Restaurant restaurant = restaurantRepository
                .findByIdAndActiveTrue(req.getRestaurantId())
                .orElseThrow(() -> AppException.notFound("Restaurant not found"));

        Cart cart = loadCart(userId);

        // Другой ресторан — сбрасываем корзину
        if (cart.getRestaurantId() != null
                && !cart.getRestaurantId().equals(req.getRestaurantId())) {
            cart = new Cart();
            cart.setItems(new ArrayList<>());
        }

        cart.setRestaurantId(restaurant.getId());
        cart.setRestaurantName(restaurant.getName());

        // Сборка опций
        List<CartItem.CartItemOption> options = buildOptions(req.getOptions());

        // Одинаковый товар+опции — увеличиваем количество
        boolean merged = false;
        for (CartItem existing : cart.getItems()) {
            if (existing.getMenuItemId().equals(req.getMenuItemId())
                    && optionsMatch(existing.getOptions(), options)) {
                existing.setQuantity(existing.getQuantity() + req.getQuantity());
                merged = true;
                break;
            }
        }

        if (!merged) {
            CartItem newItem = CartItem.builder()
                    .menuItemId(menuItem.getId())
                    .title(menuItem.getTitle())
                    .unitPrice(menuItem.getPrice())
                    .quantity(req.getQuantity())
                    .options(options)
                    .build();
            cart.getItems().add(newItem);
        }

        saveCart(userId, cart);
        return CartDto.CartResponse.from(cart);
    }

    // ── Update quantity ───────────────────────────────────

    public CartDto.CartResponse updateQuantity(UUID userId, CartDto.UpdateQuantityRequest req) {
        Cart cart = loadCart(userId);

        if (req.getQuantity() == 0) {
            cart.getItems().removeIf(i -> i.getMenuItemId().equals(req.getMenuItemId()));
        } else {
            cart.getItems().stream()
                    .filter(i -> i.getMenuItemId().equals(req.getMenuItemId()))
                    .findFirst()
                    .ifPresent(i -> i.setQuantity(req.getQuantity()));
        }

        saveCart(userId, cart);
        return CartDto.CartResponse.from(cart);
    }

    // ── Clear ─────────────────────────────────────────────

    public void clearCart(UUID userId) {
        redisTemplate.delete(CART_PREFIX + userId);
    }

    // ── Helpers ───────────────────────────────────────────

    public Cart loadCart(UUID userId) {
        String json = redisTemplate.opsForValue().get(CART_PREFIX + userId);
        if (json == null) {
            Cart empty = new Cart();
            empty.setItems(new ArrayList<>());
            return empty;
        }
        try {
            return objectMapper.readValue(json, Cart.class);
        } catch (Exception e) {
            log.error("Failed to deserialize cart for user {}", userId, e);
            Cart empty = new Cart();
            empty.setItems(new ArrayList<>());
            return empty;
        }
    }

    private void saveCart(UUID userId, Cart cart) {
        try {
            String json = objectMapper.writeValueAsString(cart);
            redisTemplate.opsForValue().set(CART_PREFIX + userId, json, CART_TTL);
        } catch (Exception e) {
            log.error("Failed to serialize cart for user {}", userId, e);
            throw AppException.badRequest("Failed to save cart");
        }
    }

    private List<CartItem.CartItemOption> buildOptions(List<CartDto.OptionRequest> optionReqs) {
        if (optionReqs == null) return new ArrayList<>();
        return optionReqs.stream()
                .map(o -> CartItem.CartItemOption.builder()
                        .optionId(o.getOptionId())
                        .name(o.getName())
                        .extraPrice(o.getExtraPrice())
                        .build())
                .toList();
    }

    private boolean optionsMatch(List<CartItem.CartItemOption> a,
                                  List<CartItem.CartItemOption> b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) {
            if (!a.get(i).getOptionId().equals(b.get(i).getOptionId())) return false;
        }
        return true;
    }
}
