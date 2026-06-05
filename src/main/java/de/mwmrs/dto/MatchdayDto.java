package de.mwmrs.dto;

import de.mwmrs.entity.Matchday;

public record MatchdayDto(
        Long id,
        Long competitionId,
        Integer number) {

    public static MatchdayDto from(Matchday m) {
        return new MatchdayDto(m.id, m.competition.id, m.number);
    }
}
