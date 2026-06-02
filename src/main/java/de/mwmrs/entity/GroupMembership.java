package de.mwmrs.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "group_membership")
public class GroupMembership extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    public Group group;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    public AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public GroupRole role = GroupRole.MEMBER;

    @Column(nullable = false)
    public boolean approved = false;

    @Column(name = "joined_at", nullable = false)
    public OffsetDateTime joinedAt = OffsetDateTime.now();

    public static GroupMembership findByGroupAndUser(Long groupId, Long userId) {
        return find("group.id = ?1 and user.id = ?2", groupId, userId).firstResult();
    }

    public static List<GroupMembership> listByGroup(Long groupId) {
        return list("group.id", groupId);
    }
}
