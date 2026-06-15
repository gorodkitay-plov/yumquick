package com.yumquick.user;

import com.yumquick.common.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserAddressRepository addressRepository;

    // ── Profile ───────────────────────────────────────────

    public UserDto.ProfileResponse getProfile(UUID userId) {
        return UserDto.ProfileResponse.from(findUser(userId));
    }

    @Transactional
    public UserDto.ProfileResponse updateProfile(UUID userId,
                                                  UserDto.UpdateProfileRequest request) {
        User user = findUser(userId);

        if (request.getPhone() != null
                && !request.getPhone().equals(user.getPhone())
                && userRepository.existsByPhone(request.getPhone())) {
            throw AppException.conflict("Phone already in use");
        }

        user.updateProfile(request.getName(), request.getPhone());
        return UserDto.ProfileResponse.from(user);
    }

    // ── Addresses ─────────────────────────────────────────

    public List<UserDto.AddressResponse> getAddresses(UUID userId) {
        return addressRepository.findByUserId(userId)
                .stream()
                .map(UserDto.AddressResponse::from)
                .toList();
    }

    @Transactional
    public UserDto.AddressResponse addAddress(UUID userId,
                                              UserDto.AddAddressRequest request) {
        User user = findUser(userId);

        if (request.isSetAsDefault()) {
            addressRepository.clearDefaultByUserId(userId);
        }

        UserAddress address = UserAddress.create(
                user,
                request.getLabel(),
                request.getDetailAddress(),
                request.getLat(),
                request.getLng()
        );

        if (request.isSetAsDefault()) {
            address.setAsDefault();
        }

        addressRepository.save(address);
        return UserDto.AddressResponse.from(address);
    }

    @Transactional
    public UserDto.AddressResponse updateAddress(UUID userId, UUID addressId,
                                                  UserDto.UpdateAddressRequest request) {
        UserAddress address = findAddress(userId, addressId);
        address.update(request.getLabel(), request.getDetailAddress(),
                request.getLat(), request.getLng());
        return UserDto.AddressResponse.from(address);
    }

    @Transactional
    public void deleteAddress(UUID userId, UUID addressId) {
        UserAddress address = findAddress(userId, addressId);
        addressRepository.delete(address);
    }

    @Transactional
    public UserDto.AddressResponse setDefaultAddress(UUID userId, UUID addressId) {
        findAddress(userId, addressId); // validate ownership
        addressRepository.clearDefaultByUserId(userId);
        UserAddress address = findAddress(userId, addressId);
        address.setAsDefault();
        return UserDto.AddressResponse.from(address);
    }

    // ── Helpers ───────────────────────────────────────────

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> AppException.notFound("User not found"));
    }

    private UserAddress findAddress(UUID userId, UUID addressId) {
        return addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> AppException.notFound("Address not found"));
    }
}
