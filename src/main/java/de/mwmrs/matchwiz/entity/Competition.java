package de.mwmrs.matchwiz.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDate;

@Entity
public class Competition extends BaseEntity {

    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    public String season;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public CompetitionStatus status = CompetitionStatus.DRAFT;

    @Column(name = "start_date")
    public LocalDate startDate;

    @Column(name = "end_date")
    public LocalDate endDate;
}
