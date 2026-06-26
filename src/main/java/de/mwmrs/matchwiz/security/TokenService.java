package de.mwmrs.matchwiz.security;

import de.mwmrs.matchwiz.entity.AppUser;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.util.Set;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class TokenService {

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @ConfigProperty(name = "matchwiz.jwt.duration")
    long durationSeconds;

    /**
     * Issues a signed JWT. The {@code groups} claim carries only the global role
     * (USER / ADMIN); group-scoped GROUP_ADMIN is resolved per-request against
     * {@link de.mwmrs.matchwiz.entity.GroupMembership}, never embedded in the token.
     */
    public String issue(AppUser user) {
        return Jwt.issuer(issuer)
                .upn(user.username)
                .subject(String.valueOf(user.id))
                .groups(Set.of(user.globalRole.name()))
                .expiresIn(Duration.ofSeconds(durationSeconds))
                .sign();
    }
}
