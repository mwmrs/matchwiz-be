package de.mwmrs.matchwiz.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(@NotBlank String code) {
}
