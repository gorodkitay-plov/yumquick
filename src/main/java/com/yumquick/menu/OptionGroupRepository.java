package com.yumquick.menu;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OptionGroupRepository extends JpaRepository<OptionGroup, UUID> {

    List<OptionGroup> findByMenuItemIdOrderBySortOrder(UUID menuItemId);

    Optional<OptionGroup> findByIdAndMenuItemId(UUID id, UUID menuItemId);
}
