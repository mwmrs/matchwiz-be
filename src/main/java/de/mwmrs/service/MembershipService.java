package de.mwmrs.service;

import de.mwmrs.entity.GroupMembership;
import de.mwmrs.entity.GroupRole;
import de.mwmrs.entity.NotificationType;
import de.mwmrs.exception.BusinessException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class MembershipService {

    @Inject
    NotificationService notificationService;

    public List<GroupMembership> listMembers(Long groupId) {
        return GroupMembership.listByGroup(groupId);
    }

    /**
     * Approves a pending membership. This also activates the user's account
     * (active=true) — the contract exposes no separate account-approval endpoint,
     * so group approval doubles as registration approval (SPEC §5).
     *
     * <p>The first approved member of a group is promoted to GROUP_ADMIN. The
     * contract has no explicit promotion endpoint, so this keeps the GROUP_ADMIN
     * role (SPEC §2) reachable for self-hosted groups created by the global ADMIN.
     */
    @Transactional
    public GroupMembership approve(Long groupId, Long userId) {
        GroupMembership m = GroupMembership.findByGroupAndUser(groupId, userId);
        if (m == null) {
            throw BusinessException.notFound("Membership not found");
        }
        m.approved = true;
        m.user.active = true;
        boolean groupHasAdmin = GroupMembership.count(
                "group.id = ?1 and role = ?2 and approved = true",
                groupId, GroupRole.GROUP_ADMIN) > 0;
        if (!groupHasAdmin) {
            m.role = GroupRole.GROUP_ADMIN;
        }
        notificationService.create(m.user, NotificationType.REGISTRATION_APPROVED,
                "Registration approved",
                "Your membership in group \"" + m.group.name + "\" has been approved.");
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
