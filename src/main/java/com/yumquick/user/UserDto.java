package com.yumquick.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserDto {

    // ── Requests ──────────────────────────────────────────

    @Getter
    public static class UpdateProfileRequest {
        private String name;
        private String phone;
    }

    @Getter
    public static class AddAddressRequest {
        @NotNull
        private AddressLabel label;

        @NotBlank
        private String detailAddress;

        @NotNull
        private Double lat;

        @NotNull
        private Double lng;

        private boolean setAsDefault;
    }

    @Getter
    public static class UpdateAddressRequest {
        private AddressLabel label;
        private String detailAddress;
        private Double lat;
        private Double lng;
    }

    // ── Responses ─────────────────────────────────────────

    public record ProfileResponse(
            UUID id,
            String name,
            String email,
            String phone,
            String avatarUrl,
            Role role,
            LocalDateTime createdAt
    ) {
        public static ProfileResponse from(User user) {
            return new ProfileResponse(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getAvatarUrl(),
                    user.getRole(),
                    user.getCreatedAt()
            );
        }
    }

    public record AddressResponse(
            UUID id,
            AddressLabel label,
            String detailAddress,
            Double lat,
            Double lng,
            boolean isDefault
    ) {
        public static AddressResponse from(UserAddress address) {
            return new AddressResponse(
                    address.getId(),
                    address.getLabel(),
                    address.getDetailAddress(),
                    address.getLat(),
                    address.getLng(),
                    address.isDefaultAddress()
            );
        }
    }
}
