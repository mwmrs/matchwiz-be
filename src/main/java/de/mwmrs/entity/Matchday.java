package de.mwmrs.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.List;

@Entity
public class Matchday extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "competition_id", nullable = false)
    public Competition competition;

    @Column(nullable = false)
    public Integer number;

    public static List<Matchday> listByCompetition(Long competitionId) {
        return list("competition.id", competitionId);
    }
}
