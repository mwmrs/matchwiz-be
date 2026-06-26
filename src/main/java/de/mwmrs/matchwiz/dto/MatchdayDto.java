package de.mwmrs.matchwiz.dto;

import de.mwmrs.matchwiz.entity.Matchday;

public record MatchdayDto(
        Long id,
        Long competitionId,
        Integer number) {

    public static MatchdayDto from(Matchday m) {
        return new MatchdayDto(m.id, m.competition.id, m.number);
    }
}
