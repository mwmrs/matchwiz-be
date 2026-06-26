package de.mwmrs.matchwiz.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "scoring_rule")
public class ScoringRule extends BaseEntity {

    @OneToOne(optional = false)
    @JoinColumn(name = "competition_id", nullable = false, unique = true)
    public Competition competition;

    @Column(name = "exact_result_points", nullable = false)
    public int exactResultPoints = 5;

    @Column(name = "goal_difference_points", nullable = false)
    public int goalDifferencePoints = 3;

    @Column(name = "tendency_points", nullable = false)
    public int tendencyPoints = 2;

    public static ScoringRule findByCompetition(Long competitionId) {
        return find("competition.id", competitionId).firstResult();
    }
}
