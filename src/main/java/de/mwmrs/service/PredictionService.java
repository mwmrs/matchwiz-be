package de.mwmrs.service;

import de.mwmrs.dto.SubmitPredictionRequest;
import de.mwmrs.entity.AppUser;
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

    public List<Prediction> listForUser(Long matchdayId, Long userId) {
        return Prediction.list("match.matchday.id = ?1 and user.id = ?2", matchdayId, userId);
    }

    /**
     * Upserts the current user's predictions for a matchday. Editable until the
     * matchday deadline; submissions after the deadline are rejected.
     */
    @Transactional
    public List<Prediction> submit(Long matchdayId, AppUser user, List<SubmitPredictionRequest> requests) {
        Matchday matchday = Matchday.findById(matchdayId);
        if (matchday == null) {
            throw BusinessException.notFound("Matchday not found");
        }
        if (matchday.deadline.isBefore(OffsetDateTime.now())) {
            throw BusinessException.badRequest("Prediction deadline has passed");
        }

        for (SubmitPredictionRequest req : requests) {
            Match match = Match.findById(req.matchId());
            if (match == null || !match.matchday.id.equals(matchdayId)) {
                throw BusinessException.badRequest("Match " + req.matchId() + " is not part of this matchday");
            }
            Prediction p = Prediction.findByUserAndMatch(user.id, match.id);
            if (p == null) {
                p = new Prediction();
                p.user = user;
                p.match = match;
            }
            p.predictedHomeGoals = req.predictedHomeGoals();
            p.predictedAwayGoals = req.predictedAwayGoals();
            p.submittedAt = OffsetDateTime.now();
            p.persist();
        }
        return listForUser(matchdayId, user.id);
    }
}
