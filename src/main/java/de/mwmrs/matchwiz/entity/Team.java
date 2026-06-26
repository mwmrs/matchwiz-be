package de.mwmrs.matchwiz.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Team extends BaseEntity {

    @Column(nullable = false)
    public String name;

    @Column(name = "short_name", nullable = false)
    public String shortName;

    @Column(name = "logo_url")
    public String logoUrl;
}
