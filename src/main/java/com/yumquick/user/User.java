package com.yumquick.user;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email", unique = true),
        @Index(name = "idx_users_phone", columnList = "phone")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = "addresses")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    // null если social login
    @Column
    private String password;

    @Column(unique = true)
    private String phone;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    // Social login
    @Column(name = "provider")
    private String provider;         // google / kakao / naver / apple

    @Column(name = "provider_id")
    private String providerId;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAddress> addresses = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Factory ───────────────────────────────────────────
    public static User create(String name, String email, String encodedPassword, Role role) {
        User user = new User();
        user.name = name;
        user.email = email;
        user.password = encodedPassword;
        user.role = role;
        return user;
    }

    public static User createSocial(String name, String email, String provider,
                                    String providerId, Role role) {
        User user = new User();
        user.name = name;
        user.email = email;
        user.provider = provider;
        user.providerId = providerId;
        user.role = role;
        return user;
    }

    // ── Update methods ────────────────────────────────────
    public void updateProfile(String name, String phone) {
        if (name != null) this.name = name;
        if (phone != null) this.phone = phone;
    }

    public void updateAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public void changeRole(Role role) {
        this.role = role;
    }
}
