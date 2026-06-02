package de.mwmrs.dto;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record CreateMatchdayRequest(
        @NotNull Long competitionId,
        @NotNull Integer number,
        @NotNull OffsetDateTime deadline) {
}
