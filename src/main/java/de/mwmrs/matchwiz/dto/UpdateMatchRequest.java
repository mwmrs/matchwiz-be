package de.mwmrs.dto;

import de.mwmrs.entity.MatchStage;
import de.mwmrs.entity.MatchStatus;
import java.time.OffsetDateTime;

public record UpdateMatchRequest(
        Long homeTeamId,
        Long awayTeamId,
        OffsetDateTime kickoffTime,
        Integer homeGoals,
        Integer awayGoals,
        MatchStatus status,
        MatchStage stage) {
}
