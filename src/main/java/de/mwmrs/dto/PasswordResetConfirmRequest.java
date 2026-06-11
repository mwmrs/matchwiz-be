package de.mwmrs.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetConfirmRequest(
        @NotBlank String code,
        @NotBlank String newPassword) {
}
