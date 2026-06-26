package de.mwmrs.matchwiz.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
public class Prediction extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    public AppUser user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    public Group group;

    @ManyToOne(optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    public Match match;

    @Column(name = "predicted_home_goals", nullable = false)
    public Integer predictedHomeGoals;

    @Column(name = "predicted_away_goals", nullable = false)
    public Integer predictedAwayGoals;

    @Column(name = "awarded_points")
    public Integer awardedPoints;

    @Column(name = "submitted_at", nullable = false)
    public OffsetDateTime submittedAt = OffsetDateTime.now();

    public static Prediction findByUserGroupAndMatch(Long userId, Long groupId, Long matchId) {
        return find("user.id = ?1 and group.id = ?2 and match.id = ?3", userId, groupId, matchId).firstResult();
    }

    public static List<Prediction> listByMatch(Long matchId) {
        return list("match.id", matchId);
    }
}
