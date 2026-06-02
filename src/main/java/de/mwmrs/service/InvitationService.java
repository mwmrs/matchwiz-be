package de.mwmrs.service;

import de.mwmrs.dto.CreateInvitationRequest;
import de.mwmrs.entity.AppUser;
import de.mwmrs.entity.Group;
import de.mwmrs.entity.GroupMembership;
import de.mwmrs.entity.GroupRole;
import de.mwmrs.entity.Invitation;
import de.mwmrs.entity.NotificationType;
import de.mwmrs.exception.BusinessException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.jboss.logging.Logger;

@ApplicationScoped
public class InvitationService {

    private static final Logger LOG = Logger.getLogger(InvitationService.class);
    private static final int EXPIRY_DAYS = 14;

    @Inject
    NotificationService notificationService;

    /**
     * Creates an invitation. For the MVP no email is sent; the token is returned
     * in the response and logged (stub-and-log).
     */
    @Transactional
    public Invitation create(Long groupId, CreateInvitationRequest req) {
        Group group = Group.findById(groupId);
        if (group == null) {
            throw BusinessException.notFound("Group not found");
        }
        Invitation inv = new Invitation();
        inv.group = group;
        inv.email = req.email();
        inv.token = UUID.randomUUID().toString();
        inv.expiresAt = OffsetDateTime.now().plusDays(EXPIRY_DAYS);
        inv.persist();
        LOG.infof("Invitation for %s to group %d: token=%s (email sending stubbed for MVP)",
                inv.email, groupId, inv.token);
        return inv;
    }

    /**
     * Accepts an invitation for the current user, creating a pending (approved=false)
     * membership. A GROUP_ADMIN/ADMIN must then approve it (which also activates the
     * account); see {@link MembershipService#approve}.
     */
    @Transactional
    public GroupMembership accept(String token, AppUser user) {
        Invitation inv = Invitation.findByToken(token);
        if (inv == null) {
            throw BusinessException.notFound("Invitation not found");
        }
        if (inv.acceptedAt != null) {
            throw BusinessException.badRequest("Invitation already accepted");
        }
        if (inv.expiresAt.isBefore(OffsetDateTime.now())) {
            throw BusinessException.badRequest("Invitation expired");
        }

        GroupMembership membership = GroupMembership.findByGroupAndUser(inv.group.id, user.id);
        if (membership == null) {
            membership = new GroupMembership();
            membership.group = inv.group;
            membership.user = user;
            membership.role = GroupRole.MEMBER;
            membership.approved = false;
            membership.persist();
        }
        inv.acceptedAt = OffsetDateTime.now();

        notificationService.create(user, NotificationType.INVITATION_ACCEPTED,
                "Invitation accepted",
                "You joined group \"" + inv.group.name + "\".");
        return membership;
    }
}
