package com.yumquick.menu;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class MenuDto {

    // ── Category requests ─────────────────────────────────

    @Getter
    public static class CategoryRequest {
        @NotBlank
        private String name;
        private int sortOrder;
    }

    // ── Item requests ─────────────────────────────────────

    @Getter
    public static class ItemRequest {
        @NotNull
        private UUID categoryId;

        @NotBlank
        private String title;

        private String description;

        @NotNull
        @DecimalMin("0.0")
        private BigDecimal price;

        private Integer spicyLevel;
        private Integer calories;
        private int sortOrder;

        private List<OptionGroupRequest> optionGroups;
    }

    @Getter
    public static class ItemUpdateRequest {
        private String title;
        private String description;
        private BigDecimal price;
        private Integer spicyLevel;
        private Integer calories;
        private int sortOrder;
    }

    // ── Option requests ───────────────────────────────────

    @Getter
    public static class OptionGroupRequest {
        @NotBlank
        private String name;
        private boolean required;
        private int maxSelect = 1;
        private int sortOrder;
        private List<OptionRequest> options;
    }

    @Getter
    public static class OptionRequest {
        @NotBlank
        private String name;
        @DecimalMin("0.0")
        private BigDecimal extraPrice = BigDecimal.ZERO;
        private int sortOrder;
    }

    // ── Responses ─────────────────────────────────────────

    public record CategoryResponse(
            UUID id,
            String name,
            int sortOrder,
            List<ItemResponse> items
    ) {
        public static CategoryResponse from(MenuCategory c) {
            return new CategoryResponse(
                    c.getId(), c.getName(), c.getSortOrder(),
                    c.getItems().stream().map(ItemResponse::from).toList()
            );
        }

        public static CategoryResponse fromWithoutItems(MenuCategory c) {
            return new CategoryResponse(c.getId(), c.getName(), c.getSortOrder(), List.of());
        }
    }

    public record ItemResponse(
            UUID id,
            UUID categoryId,
            String title,
            String description,
            BigDecimal price,
            String imageUrl,
            boolean available,
            Integer spicyLevel,
            Integer calories,
            int sortOrder,
            List<OptionGroupResponse> optionGroups
    ) {
        public static ItemResponse from(MenuItem item) {
            return new ItemResponse(
                    item.getId(),
                    item.getCategory().getId(),
                    item.getTitle(),
                    item.getDescription(),
                    item.getPrice(),
                    item.getImageUrl(),
                    item.isAvailable(),
                    item.getSpicyLevel(),
                    item.getCalories(),
                    item.getSortOrder(),
                    item.getOptionGroups().stream().map(OptionGroupResponse::from).toList()
            );
        }
    }

    public record OptionGroupResponse(
            UUID id,
            String name,
            boolean required,
            int maxSelect,
            List<OptionResponse> options
    ) {
        public static OptionGroupResponse from(OptionGroup g) {
            return new OptionGroupResponse(
                    g.getId(), g.getName(), g.isRequired(), g.getMaxSelect(),
                    g.getOptions().stream().map(OptionResponse::from).toList()
            );
        }
    }

    public record OptionResponse(
            UUID id,
            String name,
            BigDecimal extraPrice
    ) {
        public static OptionResponse from(MenuOption o) {
            return new OptionResponse(o.getId(), o.getName(), o.getExtraPrice());
        }
    }
}
