package de.mwmrs.dto;

import jakarta.validation.constraints.NotNull;

public record ManualBonusRequest(
        @NotNull Long userId,
        @NotNull Integer points,
        String reason) {
}
