package de.mwmrs.security;

import de.mwmrs.entity.GroupMembership;
import de.mwmrs.entity.GroupRole;
import de.mwmrs.exception.BusinessException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Enforces group-scoped authorization. Global ADMINs bypass all checks.
 * GROUP_ADMIN is determined from {@link GroupMembership}, not from the JWT.
 */
@ApplicationScoped
public class GroupAuthz {

    @Inject
    CurrentUser currentUser;

    /** Caller must be an approved member of the group (or a global ADMIN). */
    public void requireMember(Long groupId) {
        if (currentUser.isAdmin()) {
            return;
        }
        GroupMembership m = GroupMembership.findByGroupAndUser(groupId, currentUser.id());
        if (m == null || !m.approved) {
            throw BusinessException.forbidden("Not a member of this group");
        }
    }

    /** Caller must be a GROUP_ADMIN of the group (or a global ADMIN). */
    public void requireGroupAdmin(Long groupId) {
        if (currentUser.isAdmin()) {
            return;
        }
        GroupMembership m = GroupMembership.findByGroupAndUser(groupId, currentUser.id());
        if (m == null || !m.approved || m.role != GroupRole.GROUP_ADMIN) {
            throw BusinessException.forbidden("Group admin privileges required");
        }
    }
}
