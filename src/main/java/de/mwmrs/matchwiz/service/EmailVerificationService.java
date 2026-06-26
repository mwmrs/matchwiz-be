package de.mwmrs.matchwiz.service;

import de.mwmrs.matchwiz.entity.AppUser;
import de.mwmrs.matchwiz.entity.VerificationToken;
import de.mwmrs.matchwiz.entity.VerificationTokenType;
import de.mwmrs.matchwiz.exception.BusinessException;
import de.mwmrs.matchwiz.security.VerificationCodes;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import org.jboss.logging.Logger;

@ApplicationScoped
public class EmailVerificationService {

    private static final Logger LOG = Logger.getLogger(EmailVerificationService.class);
    private static final int EXPIRY_MINUTES = 30;

    @Inject
    EmailService emailService;

    @Transactional
    public void requestVerification(AppUser user) {
        if (user.email == null) {
            throw BusinessException.badRequest("No email address on account");
        }
        VerificationToken.deleteForUser(user, VerificationTokenType.EMAIL_VERIFICATION);

        String code = VerificationCodes.generate();
        VerificationToken token = new VerificationToken();
        token.user = user;
        token.tokenHash = VerificationCodes.sha256(code);
        token.type = VerificationTokenType.EMAIL_VERIFICATION;
        token.expiresAt = OffsetDateTime.now().plusMinutes(EXPIRY_MINUTES);
        token.persist();

        emailService.sendEmailVerificationCode(user, code, EXPIRY_MINUTES);
        LOG.infof("Email verification code issued for user %s", user.username);
    }

    @Transactional
    public void confirmVerification(AppUser user, String rawCode) {
        String hash = VerificationCodes.sha256(VerificationCodes.normalize(rawCode));
        VerificationToken token = VerificationToken.findValid(hash, VerificationTokenType.EMAIL_VERIFICATION);
        if (token == null || !token.user.id.equals(user.id)) {
            throw BusinessException.badRequest("Invalid or expired code");
        }
        token.user.emailVerified = true;
        VerificationToken.deleteForUser(token.user, VerificationTokenType.EMAIL_VERIFICATION);
        LOG.infof("Email verified for user %s", user.username);
    }
}
