package de.mwmrs.dto;

import de.mwmrs.entity.Theme;
import jakarta.validation.constraints.Email;

public record UpdateUserRequest(
        @Email String email,
        String preferredLanguage,
        String timezone,
        Theme theme,
        Boolean twoFactorEnabled,
        Boolean emailNotifications,
        Boolean matchdayReminders) {
}
