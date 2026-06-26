package de.mwmrs.matchwiz.dto;

import de.mwmrs.matchwiz.entity.ManualBonus;
import java.time.OffsetDateTime;

public record ManualBonusDto(
        Long id,
        Long userId,
        String username,
        int points,
        String reason,
        String awardedBy,
        OffsetDateTime awardedAt) {

    public static ManualBonusDto from(ManualBonus mb) {
        return new ManualBonusDto(
                mb.id,
                mb.user.id,
                mb.user.username,
                mb.points,
                mb.reason,
                mb.awardedBy.username,
                mb.awardedAt);
    }
}
