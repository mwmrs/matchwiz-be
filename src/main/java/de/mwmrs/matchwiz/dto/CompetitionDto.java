package de.mwmrs.dto;

import de.mwmrs.entity.Competition;
import de.mwmrs.entity.CompetitionStatus;
import java.time.LocalDate;

public record CompetitionDto(
        Long id,
        String name,
        String season,
        CompetitionStatus status,
        LocalDate startDate,
        LocalDate endDate) {

    public static CompetitionDto from(Competition c) {
        return new CompetitionDto(c.id, c.name, c.season, c.status, c.startDate, c.endDate);
    }
}
