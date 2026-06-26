package de.mwmrs.matchwiz.dto;

import de.mwmrs.matchwiz.entity.MatchStage;
import de.mwmrs.matchwiz.entity.MatchStatus;
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
