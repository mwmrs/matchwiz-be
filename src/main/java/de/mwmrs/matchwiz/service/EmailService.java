package de.mwmrs.matchwiz.service;

import de.mwmrs.matchwiz.entity.AppUser;
import de.mwmrs.matchwiz.entity.Match;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
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

    @Inject
    Messages messages;

    /**
     * Sends a plain-text email. Failures are logged, never propagated, so that
     * public endpoints (e.g. password reset) cannot be used as an SMTP-error oracle.
     */
    public void send(String to, String subject, String body) {
        try {
            mailer.send(Mail.withText(to, subject, body));
            LOG.infof("Email was sent to %s (subject: %s)", to, subject);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to send email to %s (subject: %s)", to, subject);
        }
    }

    public void sendUserRegistrationPending(AppUser admin, AppUser newUser) {
        String lang = admin.preferredLanguage;
        String subject = messages.get("email.user_registration_pending.subject", lang);
        String body = messages.get("email.user_registration_pending.body", lang,
                admin.username, newUser.username, newUser.email);
        send(admin.email, subject, body);
    }

    public void sendGroupJoinPending(AppUser recipient, AppUser requester, String groupName) {
        String lang = recipient.preferredLanguage;
        String subject = messages.get("email.group_join_pending.subject", lang);
        String body = messages.get("email.group_join_pending.body", lang,
                recipient.username, requester.username, groupName);
        send(recipient.email, subject, body);
    }

    public void sendPasswordResetCode(AppUser user, String code, int expiryMinutes) {
        String lang = user.preferredLanguage;
        String subject = messages.get("email.password_reset.subject", lang);
        String body = messages.get("email.password_reset.body", lang,
                user.username, user.username, code, expiryMinutes);
        send(user.email, subject, body);
    }

    public void sendMatchdayReminder(AppUser user, List<Match> matches) {
        String lang = user.preferredLanguage;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 'UTC'");
        String matchLines = matches.stream()
                .map(m -> messages.get("email.matchday_reminder.match_line", lang,
                        m.homeTeam.name, m.awayTeam.name,
                        m.kickoffTime.withOffsetSameInstant(java.time.ZoneOffset.UTC).format(fmt)))
                .collect(Collectors.joining("\n"));
        String subject = messages.get("email.matchday_reminder.subject", lang);
        String body = messages.get("email.matchday_reminder.body", lang, user.username, matchLines);
        send(user.email, subject, body);
    }
}
