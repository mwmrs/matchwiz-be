package de.mwmrs.dto;

import de.mwmrs.entity.Matchday;
import java.time.OffsetDateTime;

public record MatchdayDto(
        Long id,
        Long competitionId,
        Integer number,
        OffsetDateTime deadline) {

    public static MatchdayDto from(Matchday m) {
        return new MatchdayDto(m.id, m.competition.id, m.number, m.deadline);
    }
}
