package de.mwmrs.matchwiz.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_group")
public class Group extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "competition_id", nullable = false)
    public Competition competition;

    @Column(nullable = false)
    public String name;

    public String description;
}
