package de.mwmrs.matchwiz.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "legal_notice")
public class LegalNotice extends BaseEntity {

    @Column(nullable = false)
    public String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    public String address;

    @Column(nullable = false)
    public String telephone;

    @Column(nullable = false)
    public String email;

    public static LegalNotice get() {
        return findById(1L);
    }
}
