package de.mwmrs.dto;

import de.mwmrs.entity.Invitation;
import java.time.OffsetDateTime;

public record InvitationDto(
        Long id,
        Long groupId,
        String email,
        String token,
        OffsetDateTime expiresAt,
        OffsetDateTime acceptedAt) {

    public static InvitationDto from(Invitation i) {
        return new InvitationDto(i.id, i.group.id, i.email, i.token, i.expiresAt, i.acceptedAt);
    }
}
