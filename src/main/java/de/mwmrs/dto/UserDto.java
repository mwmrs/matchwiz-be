package de.mwmrs.dto;

import de.mwmrs.entity.AppUser;
import de.mwmrs.entity.GlobalRole;
import de.mwmrs.entity.Theme;
import java.time.OffsetDateTime;

public record UserDto(
        Long id,
        String username,
        String email,
        Boolean emailVerified,
        String preferredLanguage,
        String timezone,
        Theme theme,
        Boolean twoFactorEnabled,
        Boolean emailNotifications,
        Boolean matchdayReminders,
        GlobalRole globalRole,
        boolean active,
        OffsetDateTime createdAt) {

    public static UserDto from(AppUser u) {
        return new UserDto(
                u.id,
                u.username,
                u.email,
                u.emailVerified,
                u.preferredLanguage,
                u.timezone,
                u.theme,
                u.twoFactorEnabled,
                u.emailNotifications,
                u.matchdayReminders,
                u.globalRole,
                u.active,
                u.createdAt);
    }
}
