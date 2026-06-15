package com.yumquick.menu;

import com.yumquick.common.exception.AppException;
import com.yumquick.restaurant.Restaurant;
import com.yumquick.restaurant.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuService {

    private final MenuCategoryRepository categoryRepository;
    private final MenuItemRepository itemRepository;
    private final OptionGroupRepository optionGroupRepository;
    private final RestaurantRepository restaurantRepository;

    // ── Public: 레스토랑 전체 메뉴 조회 ──────────────────

    public List<MenuDto.CategoryResponse> getMenu(UUID restaurantId) {
        List<MenuCategory> categories = categoryRepository
                .findByRestaurantIdAndActiveTrueOrderBySortOrder(restaurantId);
        return categories.stream().map(MenuDto.CategoryResponse::from).toList();
    }

    public MenuDto.ItemResponse getItem(UUID itemId) {
        MenuItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> AppException.notFound("Menu item not found"));
        return MenuDto.ItemResponse.from(item);
    }

    // ── Owner: 카테고리 관리 ──────────────────────────────

    @Transactional
    public MenuDto.CategoryResponse createCategory(UUID ownerId, UUID restaurantId,
                                                    MenuDto.CategoryRequest req) {
        Restaurant restaurant = findOwnedRestaurant(restaurantId, ownerId);
        MenuCategory category = MenuCategory.create(restaurant, req.getName(), req.getSortOrder());
        categoryRepository.save(category);
        return MenuDto.CategoryResponse.fromWithoutItems(category);
    }

    @Transactional
    public MenuDto.CategoryResponse updateCategory(UUID ownerId, UUID restaurantId,
                                                    UUID categoryId, MenuDto.CategoryRequest req) {
        findOwnedRestaurant(restaurantId, ownerId);
        MenuCategory category = findCategory(categoryId, restaurantId);
        category.update(req.getName(), req.getSortOrder());
        return MenuDto.CategoryResponse.fromWithoutItems(category);
    }

    @Transactional
    public void deleteCategory(UUID ownerId, UUID restaurantId, UUID categoryId) {
        findOwnedRestaurant(restaurantId, ownerId);
        MenuCategory category = findCategory(categoryId, restaurantId);
        category.deactivate();
    }

    // ── Owner: 메뉴 아이템 관리 ───────────────────────────

    @Transactional
    public MenuDto.ItemResponse createItem(UUID ownerId, UUID restaurantId,
                                           MenuDto.ItemRequest req) {
        findOwnedRestaurant(restaurantId, ownerId);
        MenuCategory category = findCategory(req.getCategoryId(), restaurantId);

        MenuItem item = MenuItem.create(category, req.getTitle(),
                req.getDescription(), req.getPrice(), req.getSortOrder());
        itemRepository.save(item);

        if (req.getOptionGroups() != null) {
            saveOptionGroups(item, req.getOptionGroups());
        }

        return MenuDto.ItemResponse.from(item);
    }

    @Transactional
    public MenuDto.ItemResponse updateItem(UUID ownerId, UUID restaurantId,
                                           UUID itemId, MenuDto.ItemUpdateRequest req) {
        findOwnedRestaurant(restaurantId, ownerId);
        MenuItem item = findItem(itemId, restaurantId);
        item.update(req.getTitle(), req.getDescription(), req.getPrice(),
                req.getSpicyLevel(), req.getCalories(), req.getSortOrder());
        return MenuDto.ItemResponse.from(item);
    }

    @Transactional
    public void toggleAvailable(UUID ownerId, UUID restaurantId,
                                 UUID itemId, boolean available) {
        findOwnedRestaurant(restaurantId, ownerId);
        MenuItem item = findItem(itemId, restaurantId);
        item.setAvailable(available);
    }

    @Transactional
    public void deleteItem(UUID ownerId, UUID restaurantId, UUID itemId) {
        findOwnedRestaurant(restaurantId, ownerId);
        MenuItem item = findItem(itemId, restaurantId);
        itemRepository.delete(item);
    }

    // ── Helpers ───────────────────────────────────────────

    private Restaurant findOwnedRestaurant(UUID restaurantId, UUID ownerId) {
        return restaurantRepository.findByIdAndOwnerId(restaurantId, ownerId)
                .orElseThrow(() -> AppException.forbidden("Not your restaurant"));
    }

    private MenuCategory findCategory(UUID categoryId, UUID restaurantId) {
        return categoryRepository.findByIdAndRestaurantId(categoryId, restaurantId)
                .orElseThrow(() -> AppException.notFound("Category not found"));
    }

    private MenuItem findItem(UUID itemId, UUID restaurantId) {
        return itemRepository.findByIdAndCategoryRestaurantId(itemId, restaurantId)
                .orElseThrow(() -> AppException.notFound("Menu item not found"));
    }

    private void saveOptionGroups(MenuItem item, List<MenuDto.OptionGroupRequest> groupReqs) {
        groupReqs.forEach(groupReq -> {
            OptionGroup group = OptionGroup.create(item, groupReq.getName(),
                    groupReq.isRequired(), groupReq.getMaxSelect(), groupReq.getSortOrder());
            optionGroupRepository.save(group);

            if (groupReq.getOptions() != null) {
                groupReq.getOptions().forEach(optReq -> {
                    MenuOption option = MenuOption.create(group, optReq.getName(),
                            optReq.getExtraPrice(), optReq.getSortOrder());
                    group.getOptions().add(option);
                });
            }
        });
    }
}
