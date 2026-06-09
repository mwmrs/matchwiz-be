package de.mwmrs.dto;

import de.mwmrs.entity.GroupMembership;
import de.mwmrs.entity.GroupRole;
import java.time.OffsetDateTime;

public record GroupMembershipDto(
        Long id,
        Long groupId,
        Long userId,
        String username,
        GroupRole role,
        boolean approved,
        OffsetDateTime joinedAt,
        Integer predictionsCount) {

    public static GroupMembershipDto from(GroupMembership m) {
        return new GroupMembershipDto(
                m.id,
                m.group.id,
                m.user.id,
                m.user.username,
                m.role,
                m.approved,
                m.joinedAt,
                null);
    }

    public static GroupMembershipDto from(GroupMembership m, int predictionsCount) {
        return new GroupMembershipDto(
                m.id,
                m.group.id,
                m.user.id,
                m.user.username,
                m.role,
                m.approved,
                m.joinedAt,
                predictionsCount);
    }
}
