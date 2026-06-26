package de.mwmrs.dto;

import de.mwmrs.entity.MatchStage;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record CreateMatchRequest(
        @NotNull Long homeTeamId,
        @NotNull Long awayTeamId,
        @NotNull OffsetDateTime kickoffTime,
        MatchStage stage) {
}
