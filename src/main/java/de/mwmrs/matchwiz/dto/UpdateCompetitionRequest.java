package de.mwmrs.dto;

import de.mwmrs.entity.CompetitionStatus;
import java.time.LocalDate;

public record UpdateCompetitionRequest(
        String name,
        String season,
        CompetitionStatus status,
        LocalDate startDate,
        LocalDate endDate) {
}
