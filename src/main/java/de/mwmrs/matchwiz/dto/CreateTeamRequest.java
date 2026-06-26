package de.mwmrs.matchwiz.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateTeamRequest(
        @NotBlank String name,
        @NotBlank String shortName,
        String logoUrl) {
}
