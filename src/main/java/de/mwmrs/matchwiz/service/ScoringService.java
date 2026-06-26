package de.mwmrs.matchwiz.service;

import de.mwmrs.matchwiz.entity.Match;
import de.mwmrs.matchwiz.entity.MatchStatus;
import de.mwmrs.matchwiz.entity.Prediction;
import de.mwmrs.matchwiz.entity.ScoringRule;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Computes prediction points. See SPEC §7:
 * exact result, correct goal difference, correct tendency, otherwise zero.
 */
@ApplicationScoped
public class ScoringService {

    /** Pure scoring function. */
    public int computePoints(ScoringRule rule,
                             int predictedHome, int predictedAway,
                             int actualHome, int actualAway) {
        if (predictedHome == actualHome && predictedAway == actualAway) {
            return rule.exactResultPoints;
        }
        if (predictedHome - predictedAway == actualHome - actualAway) {
            return rule.goalDifferencePoints;
        }
        if (Integer.signum(predictedHome - predictedAway) == Integer.signum(actualHome - actualAway)) {
            return rule.tendencyPoints;
        }
        return 0;
    }

    /**
     * Recomputes awarded points for every prediction on the match. When the match
     * is not FINISHED or LIVE (or has no result), awarded points are cleared. Idempotent.
     */
    public void rescore(Match match, ScoringRule rule) {
        List<Prediction> predictions = Prediction.listByMatch(match.id);
        boolean scored = (match.status == MatchStatus.FINISHED || match.status == MatchStatus.LIVE)
                && match.homeGoals != null && match.awayGoals != null;
        for (Prediction p : predictions) {
            if (scored) {
                p.awardedPoints = computePoints(rule,
                        p.predictedHomeGoals, p.predictedAwayGoals,
                        match.homeGoals, match.awayGoals);
            } else {
                p.awardedPoints = null;
            }
        }
    }
}
