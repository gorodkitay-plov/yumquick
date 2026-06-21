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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService 테스트")
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock UserRepository userRepository;
    @Mock UserAddressRepository addressRepository;
    @Mock RestaurantRepository restaurantRepository;
    @Mock MenuItemRepository menuItemRepository;
    @Mock CartService cartService;

    @InjectMocks OrderService orderService;

    @Test
    @DisplayName("빈 장바구니로 주문하면 400 에러")
    void checkout_emptyCart_throwsBadRequest() {
        // given
        UUID userId = UUID.randomUUID();
        OrderDto.CheckoutRequest request = new OrderDto.CheckoutRequest();
        setField(request, "addressId", UUID.randomUUID());

        Cart emptyCart = new Cart();
        emptyCart.setItems(new ArrayList<>());
        given(cartService.loadCart(userId)).willReturn(emptyCart);

        // when & then
        assertThatThrownBy(() -> orderService.checkout(userId, request))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Cart is empty");
    }

    @Test
    @DisplayName("닫힌 레스토랑에 주문하면 400 에러")
    void checkout_restaurantClosed_throwsBadRequest() {
        // given
        UUID userId = UUID.randomUUID();
        UUID restaurantId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        OrderDto.CheckoutRequest request = new OrderDto.CheckoutRequest();
        setField(request, "addressId", addressId);

        // 장바구니에 아이템 있음
        Cart cart = new Cart();
        cart.setRestaurantId(restaurantId);
        CartItem cartItem = CartItem.builder()
                .menuItemId(UUID.randomUUID())
                .title("Test Item")
                .unitPrice(BigDecimal.valueOf(10000))
                .quantity(1)
                .options(new ArrayList<>())
                .build();
        cart.setItems(List.of(cartItem));

        User user = User.create("Test", "test@test.com", "password", com.yumquick.user.Role.USER);
        UserAddress address = UserAddress.create(user, com.yumquick.user.AddressLabel.HOME,
                "서울시", 37.5665, 126.9780);

        // 레스토랑은 닫혀 있음
        Restaurant restaurant = mock(Restaurant.class);
        given(restaurant.isOpen()).willReturn(false);

        given(cartService.loadCart(userId)).willReturn(cart);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(addressRepository.findByIdAndUserId(addressId, userId))
                .willReturn(Optional.of(address));
        given(restaurantRepository.findByIdAndActiveTrue(restaurantId))
                .willReturn(Optional.of(restaurant));
        given(menuItemRepository.findById(any()))
                .willReturn(Optional.of(mock(MenuItem.class)));

        // when & then
        assertThatThrownBy(() -> orderService.checkout(userId, request))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Restaurant is currently closed");
    }

    @Test
    @DisplayName("존재하지 않는 주문 조회하면 404 에러")
    void getMyOrder_notFound_throwsNotFound() {
        // given
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        given(orderRepository.findByIdAndUserId(orderId, userId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.getMyOrder(userId, orderId))
                .isInstanceOf(AppException.class);
    }

    // ── Helper ────────────────────────────────────────────

    private void setField(Object obj, String fieldName, Object value) {
        try {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}