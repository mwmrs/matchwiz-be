package de.mwmrs.matchwiz.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CreateCompetitionRequest(
        @NotBlank String name,
        @NotBlank String season,
        LocalDate startDate,
        LocalDate endDate) {
}
