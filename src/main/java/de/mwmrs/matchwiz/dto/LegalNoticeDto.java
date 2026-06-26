package de.mwmrs.matchwiz.dto;

import de.mwmrs.matchwiz.entity.LegalNotice;
import jakarta.validation.constraints.NotBlank;

public record LegalNoticeDto(
        @NotBlank String name,
        @NotBlank String address,
        @NotBlank String telephone,
        @NotBlank String email) {

    public static LegalNoticeDto from(LegalNotice e) {
        return new LegalNoticeDto(e.name, e.address, e.telephone, e.email);
    }
}
