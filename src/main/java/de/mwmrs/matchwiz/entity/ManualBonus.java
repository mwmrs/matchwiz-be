package de.mwmrs.matchwiz.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "manual_bonus")
public class ManualBonus extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    public Group group;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    public AppUser user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "awarded_by", nullable = false)
    public AppUser awardedBy;

    @Column(nullable = false)
    public int points;

    public String reason;

    @Column(name = "awarded_at", nullable = false)
    public OffsetDateTime awardedAt = OffsetDateTime.now();

    public static List<ManualBonus> listByGroup(Long groupId) {
        return list("group.id = ?1", groupId);
    }
}
