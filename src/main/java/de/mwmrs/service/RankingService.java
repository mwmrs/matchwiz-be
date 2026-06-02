package de.mwmrs.service;

import de.mwmrs.dto.RankingEntryDto;
import de.mwmrs.entity.Group;
import de.mwmrs.entity.GroupMembership;
import de.mwmrs.entity.Match;
import de.mwmrs.entity.MatchStatus;
import de.mwmrs.entity.Prediction;
import de.mwmrs.exception.BusinessException;
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
        int totalPoints;
        int exact;
        int goalDifference;
        int tendency;
    }

    /**
     * Builds the group ranking. Sorted by total points desc, then exact
     * predictions desc, then username asc (SPEC §8).
     */
    public List<RankingEntryDto> ranking(Long groupId) {
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

        List<Prediction> predictions = Prediction.list(
                "match.matchday.competition.id = ?1 and match.status = ?2",
                competitionId, MatchStatus.FINISHED);

        for (Prediction p : predictions) {
            Tally t = tallies.get(p.user.id);
            if (t == null) {
                continue; // prediction by a non-member of this group
            }
            Match match = p.match;
            if (match.homeGoals == null || match.awayGoals == null) {
                continue;
            }
            if (p.awardedPoints != null) {
                t.totalPoints += p.awardedPoints;
            }
            switch (category(p.predictedHomeGoals, p.predictedAwayGoals, match.homeGoals, match.awayGoals)) {
                case EXACT -> t.exact++;
                case GOAL_DIFFERENCE -> t.goalDifference++;
                case TENDENCY -> t.tendency++;
                case NONE -> {
                }
            }
        }

        List<Tally> sorted = new ArrayList<>(tallies.values());
        sorted.sort(Comparator
                .comparingInt((Tally t) -> t.totalPoints).reversed()
                .thenComparing(Comparator.comparingInt((Tally t) -> t.exact).reversed())
                .thenComparing((Tally t) -> t.username, String.CASE_INSENSITIVE_ORDER));

        List<RankingEntryDto> result = new ArrayList<>(sorted.size());
        int rank = 1;
        for (Tally t : sorted) {
            result.add(new RankingEntryDto(rank++, t.userId, t.username,
                    t.totalPoints, t.exact, t.goalDifference, t.tendency));
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
