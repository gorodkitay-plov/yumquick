package com.yumquick.user;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "user_addresses", indexes = {
        @Index(name = "idx_user_addresses_user_id", columnList = "user_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AddressLabel label;

    @Column(name = "detail_address", nullable = false)
    private String detailAddress;

    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lng;

    @Column(name = "is_default", nullable = false)
    private boolean defaultAddress = false;

    // ── Factory ───────────────────────────────────────────
    public static UserAddress create(User user, AddressLabel label,
                                     String detailAddress, Double lat, Double lng) {
        UserAddress address = new UserAddress();
        address.user = user;
        address.label = label;
        address.detailAddress = detailAddress;
        address.lat = lat;
        address.lng = lng;
        return address;
    }

    // ── Update ────────────────────────────────────────────
    public void update(AddressLabel label, String detailAddress, Double lat, Double lng) {
        if (label != null) this.label = label;
        if (detailAddress != null) this.detailAddress = detailAddress;
        if (lat != null) this.lat = lat;
        if (lng != null) this.lng = lng;
    }

    public void setAsDefault() {
        this.defaultAddress = true;
    }

    public void unsetDefault() {
        this.defaultAddress = false;
    }
}
