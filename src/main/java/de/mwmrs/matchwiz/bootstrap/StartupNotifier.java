package de.mwmrs.matchwiz.bootstrap;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import de.mwmrs.matchwiz.service.EmailService;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

/**
 * Sends a one-time startup notification to MATCHWIZ_NOTIFY_EMAIL when the
 * application starts. Opt-in: if the property is blank (default), no mail is
 * sent and existing dev/test behaviour is unchanged.
 */
@ApplicationScoped
public class StartupNotifier {

    private static final Logger LOG = Logger.getLogger(StartupNotifier.class);

    @ConfigProperty(name = "matchwiz.notify.email")
    Optional<String> notifyEmail;

    @ConfigProperty(name = "quarkus.application.version", defaultValue = "unknown")
    String appVersion;

    @ConfigProperty(name = "quarkus.http.cors.origins")
    String corsOrigins;

    @ConfigProperty(name = "quarkus.mailer.mock", defaultValue = "false")
    boolean mailerMock;

    @Inject
    LaunchMode launchMode;

    @Inject
    EmailService emailService;

    void onStart(@Observes StartupEvent event) {
        LOG.infof("App started, launch mode %s, appVersion %s", launchMode.getDefaultProfile(), appVersion);
        if (notifyEmail.isEmpty()) {
            return;
        }
        String body = """
                MatchWiz started.

                Version  : %s
                Profile  : %s
                Started  : %s
                CORS     : %s
                Mailer   : %s
                """.formatted(
                appVersion,
                launchMode.getDefaultProfile(),
                OffsetDateTime.now(),
                corsOrigins,
                mailerMock ? "mock (emails logged, not sent)" : "SMTP");
        emailService.send(notifyEmail.get(), "MatchWiz started", body);
    }
}
