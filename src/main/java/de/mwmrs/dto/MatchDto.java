package de.mwmrs.dto;

import de.mwmrs.entity.Match;
import de.mwmrs.entity.MatchStage;
import de.mwmrs.entity.MatchStatus;

public record MatchDto(
        Long id,
        Long matchdayId,
        Long homeTeamId,
        Long awayTeamId,
        TeamDto homeTeam,
        TeamDto awayTeam,
        java.time.OffsetDateTime kickoffTime,
        Integer homeGoals,
        Integer awayGoals,
        MatchStatus status,
        MatchStage stage) {

    public static MatchDto from(Match m) {
        return new MatchDto(
                m.id,
                m.matchday.id,
                m.homeTeam.id,
                m.awayTeam.id,
                TeamDto.from(m.homeTeam),
                TeamDto.from(m.awayTeam),
                m.kickoffTime,
                m.homeGoals,
                m.awayGoals,
                m.status,
                m.stage);
    }
}
