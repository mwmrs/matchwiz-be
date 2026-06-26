package de.mwmrs.matchwiz.dto;

import de.mwmrs.matchwiz.entity.CompetitionStatus;
import java.time.LocalDate;

public record UpdateCompetitionRequest(
        String name,
        String season,
        CompetitionStatus status,
        LocalDate startDate,
        LocalDate endDate) {
}
