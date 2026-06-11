package de.mwmrs.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * A short-lived, single-use verification code sent to a user by email.
 * Only the SHA-256 hash of the code is stored; the plaintext code is an
 * account-takeover credential and never persisted.
 */
@Entity
@Table(name = "verification_token")
public class VerificationToken extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    public AppUser user;

    @Column(name = "token_hash", nullable = false, unique = true)
    public String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public VerificationTokenType type;

    @Column(name = "expires_at", nullable = false)
    public OffsetDateTime expiresAt;

    @Column(name = "used_at")
    public OffsetDateTime usedAt;

    @Column(name = "created_at", nullable = false)
    public OffsetDateTime createdAt = OffsetDateTime.now();

    public static VerificationToken findValid(String tokenHash, VerificationTokenType type) {
        return find("tokenHash = ?1 and type = ?2 and usedAt is null and expiresAt > ?3",
                tokenHash, type, OffsetDateTime.now()).firstResult();
    }

    public static void deleteForUser(AppUser user, VerificationTokenType type) {
        delete("user = ?1 and type = ?2", user, type);
    }
}
