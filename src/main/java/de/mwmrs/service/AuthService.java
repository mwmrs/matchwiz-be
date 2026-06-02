package de.mwmrs.service;

import de.mwmrs.dto.LoginRequest;
import de.mwmrs.dto.LoginResponse;
import de.mwmrs.dto.RegisterRequest;
import de.mwmrs.dto.UserDto;
import de.mwmrs.entity.AppUser;
import de.mwmrs.entity.GlobalRole;
import de.mwmrs.exception.BusinessException;
import de.mwmrs.security.PasswordService;
import de.mwmrs.security.TokenService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AuthService {

    @Inject
    PasswordService passwordService;

    @Inject
    TokenService tokenService;

    /**
     * Registers a new user. The account is created inactive (active=false) and
     * must be approved by an ADMIN or GROUP_ADMIN before login is permitted.
     */
    @Transactional
    public AppUser register(RegisterRequest req) {
        if (AppUser.findByUsername(req.username()) != null) {
            throw BusinessException.conflict("Username already taken");
        }
        AppUser user = new AppUser();
        user.username = req.username();
        user.passwordHash = passwordService.hash(req.password());
        user.email = req.email();
        user.globalRole = GlobalRole.USER;
        user.active = false;
        user.persist();
        return user;
    }

    /**
     * Authenticates a user. A token is issued even when the account is not yet
     * approved (active=false) so the user can accept group invitations; group
     * access remains gated until a GROUP_ADMIN/ADMIN approves the membership
     * (which also activates the account). See {@link MembershipService#approve}.
     */
    public LoginResponse login(LoginRequest req) {
        AppUser user = AppUser.findByUsername(req.username());
        if (user == null || !passwordService.matches(req.password(), user.passwordHash)) {
            throw BusinessException.unauthorized("Invalid credentials");
        }
        String token = tokenService.issue(user);
        return new LoginResponse(token, UserDto.from(user));
    }
}
