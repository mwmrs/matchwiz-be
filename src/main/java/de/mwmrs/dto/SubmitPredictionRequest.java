package de.mwmrs.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record SubmitPredictionRequest(
        @NotNull Long matchId,
        @NotNull @PositiveOrZero Integer predictedHomeGoals,
        @NotNull @PositiveOrZero Integer predictedAwayGoals) {
}
