package de.mwmrs.service;

import de.mwmrs.dto.GroupMembershipDto;
import de.mwmrs.entity.AppUser;
import de.mwmrs.entity.Group;
import de.mwmrs.entity.GroupMembership;
import de.mwmrs.entity.GroupRole;
import de.mwmrs.entity.NotificationType;
import de.mwmrs.entity.Prediction;
import de.mwmrs.exception.BusinessException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class MembershipService {

    @Inject
    NotificationService notificationService;

    public List<GroupMembershipDto> listMembers(Long groupId) {
        List<GroupMembership> members = GroupMembership.listByGroup(groupId);
        Map<Long, Long> countByUserId = Prediction.<Prediction>list("group.id = ?1", groupId)
                .stream()
                .collect(Collectors.groupingBy(p -> p.user.id, Collectors.counting()));
        return members.stream()
                .map(m -> GroupMembershipDto.from(m, countByUserId.getOrDefault(m.user.id, 0L).intValue()))
                .toList();
    }

    public List<GroupMembership> listByUser(Long userId) {
        return GroupMembership.listByUser(userId);
    }

    /**
     * Approves a pending join request. The first approved member of a group is
     * auto-promoted to GROUP_ADMIN (SPEC §2).
     */
    @Transactional
    public GroupMembership approve(Long groupId, Long userId) {
        GroupMembership m = GroupMembership.findByGroupAndUser(groupId, userId);
        if (m == null) {
            throw BusinessException.notFound("Membership not found");
        }
        m.approved = true;
        boolean groupHasAdmin = GroupMembership.count(
                "group.id = ?1 and role = ?2 and approved = true",
                groupId, GroupRole.GROUP_ADMIN) > 0;
        if (!groupHasAdmin) {
            m.role = GroupRole.GROUP_ADMIN;
        }
        notificationService.create(m.user, NotificationType.REGISTRATION_APPROVED,
                "Membership approved",
                "Your membership in group \"" + m.group.name + "\" has been approved.");
        return m;
    }

    @Transactional
    public GroupMembership join(Long groupId, AppUser user) {
        Group group = Group.findById(groupId);
        if (group == null) {
            throw BusinessException.notFound("Group not found");
        }
        if (GroupMembership.findByGroupAndUser(groupId, user.id) != null) {
            throw BusinessException.conflict("Already a member or join request pending");
        }
        GroupMembership m = new GroupMembership();
        m.group = group;
        m.user = user;
        m.role = GroupRole.MEMBER;
        m.approved = false;
        m.persist();
        return m;
    }

    @Transactional
    public void remove(Long groupId, Long userId) {
        GroupMembership m = GroupMembership.findByGroupAndUser(groupId, userId);
        if (m == null) {
            throw BusinessException.notFound("Membership not found");
        }
        m.delete();
    }
}
