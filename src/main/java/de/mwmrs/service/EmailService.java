package de.mwmrs.service;

import de.mwmrs.entity.AppUser;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

/**
 * Thin wrapper around the Quarkus mailer. SMTP is configured via the
 * MATCHWIZ_SMTP_* env vars (see application.properties); in dev/test the
 * mock mailer is active, so mails are logged instead of sent.
 */
@ApplicationScoped
public class EmailService {

    private static final Logger LOG = Logger.getLogger(EmailService.class);

    @Inject
    Mailer mailer;

    /**
     * Sends a plain-text email. Failures are logged, never propagated, so that
     * public endpoints (e.g. password reset) cannot be used as an SMTP-error oracle.
     */
    public void send(String to, String subject, String body) {
        try {
            mailer.send(Mail.withText(to, subject, body));
        } catch (Exception e) {
            LOG.errorf(e, "Failed to send email to %s (subject: %s)", to, subject);
        }
    }

    public void sendPasswordResetCode(AppUser user, String code, int expiryMinutes) {
        String body = """
                Hi %s,

                a password reset was requested for your MatchWiz account "%s".

                Your reset code: %s

                The code is valid for %d minutes and can be used once.
                If you did not request a password reset, you can ignore this email.
                """.formatted(user.username, user.username, code, expiryMinutes);
        send(user.email, "MatchWiz password reset code", body);
    }
}
