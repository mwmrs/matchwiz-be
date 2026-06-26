package de.mwmrs.bootstrap;

import de.mwmrs.entity.AppUser;
import de.mwmrs.entity.GlobalRole;
import de.mwmrs.security.PasswordService;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Ensures an active ADMIN user exists on startup, created from configuration.
 * Idempotent: an existing user with the configured username is never modified.
 */
@ApplicationScoped
public class AdminSeeder {

    private static final Logger LOG = Logger.getLogger(AdminSeeder.class);

    @ConfigProperty(name = "matchwiz.admin.username")
    String adminUsername;

    @ConfigProperty(name = "matchwiz.admin.password")
    String adminPassword;

    @Inject
    PasswordService passwordService;

    @Transactional
    void onStart(@Observes StartupEvent event) {
        if (AppUser.findByUsername(adminUsername) != null) {
            return;
        }
        AppUser admin = new AppUser();
        admin.username = adminUsername;
        admin.passwordHash = passwordService.hash(adminPassword);
        admin.globalRole = GlobalRole.ADMIN;
        admin.active = true;
        admin.persist();
        LOG.infof("Bootstrap ADMIN user '%s' created", adminUsername);
    }
}
