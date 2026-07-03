package com.yumquick.restaurant;

import com.yumquick.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "restaurants", indexes = {
        @Index(name = "idx_restaurants_owner_id", columnList = "owner_id"),
        @Index(name = "idx_restaurants_is_open", columnList = "is_open"),
        @Index(name = "idx_restaurants_lat_lng", columnList = "lat, lng")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "cover_url")
    private String coverUrl;

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "rating_count", nullable = false)
    private int ratingCount = 0;

    @Column(name = "delivery_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Column(name = "min_order", nullable = false, precision = 10, scale = 2)
    private BigDecimal minOrder = BigDecimal.ZERO;

    @Column(name = "estimated_delivery_minutes")
    private Integer estimatedDeliveryMinutes;

    @Column(name = "is_open", nullable = false)
    private boolean open = false;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private RestaurantCategory category;

    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lng;

    @Column(name = "address_detail")
    private String addressDetail;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RestaurantHours> hours = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Factory ───────────────────────────────────────────

    public static Restaurant create(User owner, String name, String description,
                                    Double lat, Double lng, String addressDetail,
                                    BigDecimal deliveryFee, BigDecimal minOrder) {
        Restaurant r = new Restaurant();
        r.owner = owner;
        r.name = name;
        r.description = description;
        r.lat = lat;
        r.lng = lng;
        r.addressDetail = addressDetail;
        r.deliveryFee = deliveryFee;
        r.minOrder = minOrder;
        return r;
    }

    // ── Update ────────────────────────────────────────────

    public void update(String name, String description, String addressDetail,
                       BigDecimal deliveryFee, BigDecimal minOrder,
                       Integer estimatedDeliveryMinutes) {
        if (name != null) this.name = name;
        if (description != null) this.description = description;
        if (addressDetail != null) this.addressDetail = addressDetail;
        if (deliveryFee != null) this.deliveryFee = deliveryFee;
        if (minOrder != null) this.minOrder = minOrder;
        if (estimatedDeliveryMinutes != null) this.estimatedDeliveryMinutes = estimatedDeliveryMinutes;
    }

    public void updateImages(String logoUrl, String coverUrl) {
        if (logoUrl != null) this.logoUrl = logoUrl;
        if (coverUrl != null) this.coverUrl = coverUrl;
    }

    public void open() { this.open = true; }
    public void close() { this.open = false; }
    public void deactivate() { this.active = false; }
    public void activate() { this.active = true; }

    public void updateRating(BigDecimal newRating, int newCount) {
        this.rating = newRating;
        this.ratingCount = newCount;
    }
}
