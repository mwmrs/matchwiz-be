package de.mwmrs.matchwiz.service;

import de.mwmrs.matchwiz.dto.RankingEntryDto;
import de.mwmrs.matchwiz.entity.Group;
import de.mwmrs.matchwiz.entity.GroupMembership;
import de.mwmrs.matchwiz.entity.ManualBonus;
import de.mwmrs.matchwiz.entity.Match;
import de.mwmrs.matchwiz.entity.MatchStatus;
import de.mwmrs.matchwiz.entity.Prediction;
import de.mwmrs.matchwiz.exception.BusinessException;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class RankingService {

    /** Mutable accumulator per member. */
    private static final class Tally {
        Long userId;
        String username;
        int predictionPoints;
        int bonusPoints;
        int exact;
        int goalDifference;
        int tendency;

        int totalPoints() {
            return predictionPoints + bonusPoints;
        }
    }

    /**
     * Builds the group ranking. Sorted by total points desc, then exact
     * predictions desc, then username asc (SPEC §8).
     */
    public List<RankingEntryDto> ranking(Long groupId, Long matchdayId) {
        Group group = Group.findById(groupId);
        if (group == null) {
            throw BusinessException.notFound("Group not found");
        }
        Long competitionId = group.competition.id;

        Map<Long, Tally> tallies = new LinkedHashMap<>();
        List<GroupMembership> members = GroupMembership.list("group.id = ?1 and approved = true", groupId);
        for (GroupMembership m : members) {
            Tally t = new Tally();
            t.userId = m.user.id;
            t.username = m.user.username;
            tallies.put(m.user.id, t);
        }

        List<Prediction> predictions;
        if (matchdayId != null) {
            predictions = Prediction.list(
                    "group.id = ?1 and match.matchday.id = ?2 and match.status in (?3, ?4)",
                    groupId, matchdayId, MatchStatus.FINISHED, MatchStatus.LIVE);
        } else {
            predictions = Prediction.list(
                    "group.id = ?1 and match.matchday.competition.id = ?2 and match.status in (?3, ?4)",
                    groupId, competitionId, MatchStatus.FINISHED, MatchStatus.LIVE);
        }

        for (Prediction p : predictions) {
            Tally t = tallies.get(p.user.id);
            if (t == null) {
                continue; // safety net: approved membership changed since query
            }
            Match match = p.match;
            if (match.homeGoals == null || match.awayGoals == null) {
                continue;
            }
            if (p.awardedPoints != null) {
                t.predictionPoints += p.awardedPoints;
            }
            switch (category(p.predictedHomeGoals, p.predictedAwayGoals, match.homeGoals, match.awayGoals)) {
                case EXACT -> t.exact++;
                case GOAL_DIFFERENCE -> t.goalDifference++;
                case TENDENCY -> t.tendency++;
                case NONE -> {
                }
            }
        }

        if (matchdayId == null) {
            for (ManualBonus mb : ManualBonus.listByGroup(groupId)) {
                Tally t = tallies.get(mb.user.id);
                if (t != null) {
                    t.bonusPoints += mb.points;
                }
            }
        }

        List<Tally> sorted = new ArrayList<>(tallies.values());
        sorted.sort(Comparator
                .comparingInt((Tally t) -> t.totalPoints()).reversed()
                .thenComparing(Comparator.comparingInt((Tally t) -> t.exact).reversed())
                .thenComparing((Tally t) -> t.username, String.CASE_INSENSITIVE_ORDER));

        List<RankingEntryDto> result = new ArrayList<>(sorted.size());
        int rank = 1;
        for (Tally t : sorted) {
            result.add(new RankingEntryDto(rank++, t.userId, t.username,
                    t.totalPoints(), t.exact, t.goalDifference, t.tendency, t.bonusPoints));
        }
        return result;
    }

    private enum Category { EXACT, GOAL_DIFFERENCE, TENDENCY, NONE }

    private Category category(int predHome, int predAway, int home, int away) {
        if (predHome == home && predAway == away) {
            return Category.EXACT;
        }
        if (predHome - predAway == home - away) {
            return Category.GOAL_DIFFERENCE;
        }
        if (Integer.signum(predHome - predAway) == Integer.signum(home - away)) {
            return Category.TENDENCY;
        }
        return Category.NONE;
    }
}
