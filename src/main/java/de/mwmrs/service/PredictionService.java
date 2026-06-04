package de.mwmrs.service;

import de.mwmrs.dto.SubmitPredictionRequest;
import de.mwmrs.entity.AppUser;
import de.mwmrs.entity.Group;
import de.mwmrs.entity.GroupMembership;
import de.mwmrs.entity.Match;
import de.mwmrs.entity.Matchday;
import de.mwmrs.entity.Prediction;
import de.mwmrs.exception.BusinessException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;

@ApplicationScoped
public class PredictionService {

    public List<Prediction> listForUser(Long matchdayId, Long groupId, Long userId) {
        return Prediction.list(
                "match.matchday.id = ?1 and group.id = ?2 and user.id = ?3",
                matchdayId, groupId, userId);
    }

    @Transactional
    public List<Prediction> submit(Long matchdayId, Long groupId, AppUser user,
                                   List<SubmitPredictionRequest> requests) {
        Matchday matchday = Matchday.findById(matchdayId);
        if (matchday == null) {
            throw BusinessException.notFound("Matchday not found");
        }
        if (matchday.deadline.isBefore(OffsetDateTime.now())) {
            throw BusinessException.badRequest("Prediction deadline has passed");
        }

        Group group = Group.findById(groupId);
        if (group == null) {
            throw BusinessException.notFound("Group not found");
        }
        if (!group.competition.id.equals(matchday.competition.id)) {
            throw BusinessException.badRequest("Group does not belong to this matchday's competition");
        }

        GroupMembership membership = GroupMembership.findByGroupAndUser(groupId, user.id);
        if (membership == null || !membership.approved) {
            throw BusinessException.forbidden("Not an approved member of this group");
        }

        for (SubmitPredictionRequest req : requests) {
            Match match = Match.findById(req.matchId());
            if (match == null || !match.matchday.id.equals(matchdayId)) {
                throw BusinessException.badRequest("Match " + req.matchId() + " is not part of this matchday");
            }
            Prediction p = Prediction.findByUserGroupAndMatch(user.id, groupId, match.id);
            if (p == null) {
                p = new Prediction();
                p.user = user;
                p.group = group;
                p.match = match;
            }
            p.predictedHomeGoals = req.predictedHomeGoals();
            p.predictedAwayGoals = req.predictedAwayGoals();
            p.submittedAt = OffsetDateTime.now();
            p.persist();
        }
        return listForUser(matchdayId, groupId, user.id);
    }
}
