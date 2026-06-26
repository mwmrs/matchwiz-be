package de.mwmrs.matchwiz.dto;

import jakarta.validation.constraints.NotNull;

public record CreateMatchdayRequest(
        @NotNull Long competitionId,
        @NotNull Integer number) {
}
