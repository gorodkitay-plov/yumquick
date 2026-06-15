package com.yumquick.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAddressRepository extends JpaRepository<UserAddress, UUID> {

    List<UserAddress> findByUserId(UUID userId);

    Optional<UserAddress> findByIdAndUserId(UUID id, UUID userId);

    // Сбросить default у всех адресов юзера перед установкой нового
    @Modifying
    @Query("UPDATE UserAddress a SET a.defaultAddress = false WHERE a.user.id = :userId")
    void clearDefaultByUserId(UUID userId);

    Optional<UserAddress> findByUserIdAndDefaultAddressTrue(UUID userId);
}
