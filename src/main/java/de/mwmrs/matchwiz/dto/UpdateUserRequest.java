package de.mwmrs.matchwiz.dto;

import de.mwmrs.matchwiz.entity.Theme;
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
