package de.mwmrs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateGroupRequest(
        @NotNull Long competitionId,
        @NotBlank String name,
        String description) {
}
