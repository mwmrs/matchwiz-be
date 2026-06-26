package de.mwmrs.matchwiz.service;

import de.mwmrs.matchwiz.dto.CreateInvitationRequest;
import de.mwmrs.matchwiz.entity.AppUser;
import de.mwmrs.matchwiz.entity.Group;
import de.mwmrs.matchwiz.entity.GroupMembership;
import de.mwmrs.matchwiz.entity.GroupRole;
import de.mwmrs.matchwiz.entity.Invitation;
import de.mwmrs.matchwiz.entity.NotificationType;
import de.mwmrs.matchwiz.exception.BusinessException;
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

    @Inject
    EmailService emailService;

    @Inject
    Messages messages;

    /**
     * Creates an invitation and emails the token to the invitee. The token is
     * also returned in the response so it can be shared manually.
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

        String subject = messages.get("email.invitation.subject", null);
        String body = messages.get("email.invitation.body", null, group.name, inv.token, EXPIRY_DAYS);
        emailService.send(inv.email, subject, body);

        LOG.infof("Invitation for %s to group %d created", inv.email, groupId);
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

        String lang = user.preferredLanguage;
        notificationService.create(user, NotificationType.INVITATION_ACCEPTED,
                messages.get("notification.invitation_accepted.title", lang),
                messages.get("notification.invitation_accepted.message", lang, inv.group.name));
        return membership;
    }
}
