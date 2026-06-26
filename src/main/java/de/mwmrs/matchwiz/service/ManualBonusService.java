package de.mwmrs.service;

import de.mwmrs.entity.AppUser;
import de.mwmrs.entity.GlobalRole;
import de.mwmrs.entity.Group;
import de.mwmrs.entity.GroupMembership;
import de.mwmrs.entity.GroupRole;
import de.mwmrs.entity.ManualBonus;
import de.mwmrs.exception.BusinessException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class ManualBonusService {

    @Transactional
    public ManualBonus award(Long groupId, Long userId, int points, String reason, AppUser admin) {
        requireGroupAdmin(groupId, admin);

        Group group = Group.findById(groupId);
        if (group == null) {
            throw BusinessException.notFound("Group not found");
        }
        GroupMembership target = GroupMembership.findByGroupAndUser(groupId, userId);
        if (target == null || !target.approved) {
            throw BusinessException.notFound("Member not found in group");
        }

        ManualBonus mb = new ManualBonus();
        mb.group = group;
        mb.user = target.user;
        mb.awardedBy = admin;
        mb.points = points;
        mb.reason = reason;
        mb.persist();
        return mb;
    }

    @Transactional
    public void remove(Long bonusId, AppUser admin) {
        ManualBonus mb = ManualBonus.findById(bonusId);
        if (mb == null) {
            throw BusinessException.notFound("Manual bonus not found");
        }
        requireGroupAdmin(mb.group.id, admin);
        mb.delete();
    }

    public List<ManualBonus> listForGroup(Long groupId, AppUser admin) {
        requireGroupAdmin(groupId, admin);
        return ManualBonus.listByGroup(groupId);
    }

    private void requireGroupAdmin(Long groupId, AppUser user) {
        if (user.globalRole == GlobalRole.ADMIN) {
            return;
        }
        GroupMembership m = GroupMembership.findByGroupAndUser(groupId, user.id);
        if (m == null || m.role != GroupRole.GROUP_ADMIN) {
            throw BusinessException.forbidden("Group admin role required");
        }
    }
}
