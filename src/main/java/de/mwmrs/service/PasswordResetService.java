package de.mwmrs.service;

import de.mwmrs.entity.AppUser;
import de.mwmrs.entity.VerificationToken;
import de.mwmrs.entity.VerificationTokenType;
import de.mwmrs.exception.BusinessException;
import de.mwmrs.security.PasswordService;
import de.mwmrs.security.VerificationCodes;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import org.jboss.logging.Logger;

@ApplicationScoped
public class PasswordResetService {

    private static final Logger LOG = Logger.getLogger(PasswordResetService.class);
    private static final int EXPIRY_MINUTES = 30;

    @Inject
    PasswordService passwordService;

    @Inject
    EmailService emailService;

    /**
     * Issues a reset code for every account registered under the given email
     * (email is neither unique nor mandatory on app_user). Intentionally never
     * fails for unknown emails so the endpoint does not leak which addresses
     * have an account. Note: the mail is sent within the transaction; with a
     * slow SMTP server this holds a DB connection, which is acceptable at this
     * scale.
     */
    @Transactional
    public void requestReset(String email) {
        List<AppUser> users = AppUser.list("lower(email) = lower(?1)", email);
        for (AppUser user : users) {
            VerificationToken.deleteForUser(user, VerificationTokenType.PASSWORD_RESET);

            String code = VerificationCodes.generate();
            VerificationToken token = new VerificationToken();
            token.user = user;
            token.tokenHash = VerificationCodes.sha256(code);
            token.type = VerificationTokenType.PASSWORD_RESET;
            token.expiresAt = OffsetDateTime.now().plusMinutes(EXPIRY_MINUTES);
            token.persist();

            emailService.sendPasswordResetCode(user, code, EXPIRY_MINUTES);
            LOG.infof("Password reset code issued for user %s", user.username);
        }
    }

    /**
     * Validates the code and sets the new password. Responds with a uniform
     * error for unknown, expired, and already-used codes so the endpoint does
     * not reveal which condition failed.
     */
    @Transactional
    public void confirmReset(String rawCode, String newPassword) {
        String hash = VerificationCodes.sha256(VerificationCodes.normalize(rawCode));
        VerificationToken token = VerificationToken.findValid(hash, VerificationTokenType.PASSWORD_RESET);
        if (token == null) {
            throw BusinessException.badRequest("Invalid or expired code");
        }
        token.user.passwordHash = passwordService.hash(newPassword);
        VerificationToken.deleteForUser(token.user, VerificationTokenType.PASSWORD_RESET);
        LOG.infof("Password reset completed for user %s", token.user.username);
    }
}
