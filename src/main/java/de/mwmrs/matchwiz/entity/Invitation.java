package de.mwmrs.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.OffsetDateTime;

@Entity
public class Invitation extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    public Group group;

    @Column(nullable = false)
    public String email;

    @Column(nullable = false, unique = true)
    public String token;

    @Column(name = "expires_at", nullable = false)
    public OffsetDateTime expiresAt;

    @Column(name = "accepted_at")
    public OffsetDateTime acceptedAt;

    public static Invitation findByToken(String token) {
        return find("token", token).firstResult();
    }
}
