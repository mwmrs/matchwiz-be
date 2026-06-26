package de.mwmrs.matchwiz.service;

import de.mwmrs.matchwiz.dto.GroupMembershipDto;
import de.mwmrs.matchwiz.entity.AppUser;
import de.mwmrs.matchwiz.entity.CompetitionStatus;
import de.mwmrs.matchwiz.entity.Group;
import de.mwmrs.matchwiz.entity.GroupMembership;
import de.mwmrs.matchwiz.entity.GroupRole;
import de.mwmrs.matchwiz.entity.NotificationType;
import de.mwmrs.matchwiz.entity.Prediction;
import de.mwmrs.matchwiz.exception.BusinessException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class MembershipService {

    @Inject
    NotificationService notificationService;

    @Inject
    EmailService emailService;

    @Inject
    Messages messages;

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
        String lang = m.user.preferredLanguage;
        notificationService.create(m.user, NotificationType.REGISTRATION_APPROVED,
                messages.get("notification.membership_approved.title", lang),
                messages.get("notification.membership_approved.message", lang, m.group.name));
        return m;
    }

    @Transactional
    public GroupMembership join(Long groupId, AppUser user) {
        Group group = Group.findById(groupId);
        if (group == null) {
            throw BusinessException.notFound("Group not found");
        }
        if (group.competition.status != CompetitionStatus.ACTIVE) {
            throw BusinessException.conflict("Cannot join group: competition is not active");
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
        List<AppUser> recipients = new ArrayList<>(AppUser.findAllAdmins());
        GroupMembership.<GroupMembership>list(
                        "group.id = ?1 and role = ?2 and approved = true",
                        groupId, GroupRole.GROUP_ADMIN)
                .stream()
                .map(gm -> gm.user)
                .filter(u -> recipients.stream().noneMatch(r -> r.id.equals(u.id)))
                .forEach(recipients::add);
        for (AppUser recipient : recipients) {
            String lang = recipient.preferredLanguage;
            notificationService.create(recipient, NotificationType.GROUP_JOIN_PENDING,
                    messages.get("notification.group_join_pending.title", lang),
                    messages.get("notification.group_join_pending.message", lang,
                            user.username, group.name));
            if (recipient.emailNotifications && recipient.email != null) {
                emailService.sendGroupJoinPending(recipient, user, group.name);
            }
        }
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
