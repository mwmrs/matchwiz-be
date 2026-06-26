package de.mwmrs.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "app_user")
public class AppUser extends BaseEntity {

    @Column(nullable = false, unique = true)
    public String username;

    @Column(name = "password_hash", nullable = false)
    public String passwordHash;

    public String email;

    @Column(name = "email_verified", nullable = false)
    public boolean emailVerified = false;

    @Column(name = "preferred_language")
    public String preferredLanguage;

    public String timezone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Theme theme = Theme.SYSTEM;

    @Column(name = "two_factor_enabled", nullable = false)
    public boolean twoFactorEnabled = false;

    @Column(name = "email_notifications", nullable = false)
    public boolean emailNotifications = false;

    @Column(name = "matchday_reminders", nullable = false)
    public boolean matchdayReminders = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "global_role", nullable = false)
    public GlobalRole globalRole = GlobalRole.USER;

    @Column(nullable = false)
    public boolean active = false;

    @Column(name = "created_at", nullable = false)
    public OffsetDateTime createdAt = OffsetDateTime.now();

    public static AppUser findByUsername(String username) {
        return find("username", username).firstResult();
    }

    public static List<AppUser> findAllAdmins() {
        return list("globalRole", GlobalRole.ADMIN);
    }
}
