package de.mwmrs.matchwiz.service;

import de.mwmrs.matchwiz.dto.LoginRequest;
import de.mwmrs.matchwiz.dto.LoginResponse;
import de.mwmrs.matchwiz.dto.RegisterRequest;
import de.mwmrs.matchwiz.dto.UserDto;
import de.mwmrs.matchwiz.entity.AppUser;
import de.mwmrs.matchwiz.entity.GlobalRole;
import de.mwmrs.matchwiz.entity.NotificationType;
import de.mwmrs.matchwiz.exception.BusinessException;
import de.mwmrs.matchwiz.security.PasswordService;
import de.mwmrs.matchwiz.security.TokenService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class AuthService {

    @Inject
    PasswordService passwordService;

    @Inject
    TokenService tokenService;

    @Inject
    NotificationService notificationService;

    @Inject
    EmailService emailService;

    @Inject
    Messages messages;

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
        List<AppUser> admins = AppUser.findAllAdmins();
        for (AppUser admin : admins) {
            String lang = admin.preferredLanguage;
            notificationService.create(admin, NotificationType.USER_REGISTRATION_PENDING,
                    messages.get("notification.user_registration_pending.title", lang),
                    messages.get("notification.user_registration_pending.message", lang, user.username));
            if (admin.emailNotifications && admin.email != null) {
                emailService.sendUserRegistrationPending(admin, user);
            }
        }
        return user;
    }

    public LoginResponse login(LoginRequest req) {
        AppUser user = AppUser.findByUsername(req.username());
        if (user == null || !passwordService.matches(req.password(), user.passwordHash)) {
            throw BusinessException.unauthorized("Invalid credentials");
        }
        if (!user.active) {
            throw BusinessException.forbidden("Account pending approval");
        }
        String token = tokenService.issue(user);
        return new LoginResponse(token, UserDto.from(user));
    }
}
