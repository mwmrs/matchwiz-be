package de.mwmrs.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "match")
public class Match extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "matchday_id", nullable = false)
    public Matchday matchday;

    @ManyToOne(optional = false)
    @JoinColumn(name = "home_team_id", nullable = false)
    public Team homeTeam;

    @ManyToOne(optional = false)
    @JoinColumn(name = "away_team_id", nullable = false)
    public Team awayTeam;

    @Column(name = "kickoff_time", nullable = false)
    public OffsetDateTime kickoffTime;

    @Column(name = "home_goals")
    public Integer homeGoals;

    @Column(name = "away_goals")
    public Integer awayGoals;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public MatchStatus status = MatchStatus.SCHEDULED;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage")
    public MatchStage stage;

    public static List<Match> listByMatchday(Long matchdayId) {
        return list("matchday.id", matchdayId);
    }
}
