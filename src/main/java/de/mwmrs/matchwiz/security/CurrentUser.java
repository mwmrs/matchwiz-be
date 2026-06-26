package de.mwmrs.matchwiz.security;

import de.mwmrs.matchwiz.entity.AppUser;
import de.mwmrs.matchwiz.entity.GlobalRole;
import de.mwmrs.matchwiz.exception.BusinessException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

/**
 * Request-scoped access to the authenticated user, resolved from the JWT subject.
 */
@RequestScoped
public class CurrentUser {

    @Inject
    JsonWebToken jwt;

    private AppUser cached;

    public Long id() {
        String sub = jwt.getSubject();
        if (sub == null) {
            throw BusinessException.unauthorized("Not authenticated");
        }
        return Long.valueOf(sub);
    }

    public AppUser require() {
        if (cached == null) {
            cached = AppUser.findById(id());
            if (cached == null) {
                throw BusinessException.unauthorized("Unknown user");
            }
        }
        return cached;
    }

    public boolean isAdmin() {
        return require().globalRole == GlobalRole.ADMIN;
    }
}
