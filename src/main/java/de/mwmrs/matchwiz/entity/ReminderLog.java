package de.mwmrs.matchwiz.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;

@Entity
@Table(name = "reminder_log", uniqueConstraints = {
        @UniqueConstraint(name = "uq_reminder_log_user_match", columnNames = {"user_id", "match_id"})
})
public class ReminderLog extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    public AppUser user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    public Match match;

    @Column(name = "sent_at", nullable = false)
    public OffsetDateTime sentAt = OffsetDateTime.now();

    public static boolean existsByUserAndMatch(Long userId, Long matchId) {
        return count("user.id = ?1 and match.id = ?2", userId, matchId) > 0;
    }
}
