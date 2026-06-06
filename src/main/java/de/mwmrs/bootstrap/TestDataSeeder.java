package de.mwmrs.bootstrap;

import de.mwmrs.entity.AppUser;
import de.mwmrs.entity.Competition;
import de.mwmrs.entity.CompetitionStatus;
import de.mwmrs.entity.Group;
import de.mwmrs.entity.GroupMembership;
import de.mwmrs.entity.GroupRole;
import de.mwmrs.entity.Match;
import de.mwmrs.entity.MatchStatus;
import de.mwmrs.entity.Matchday;
import de.mwmrs.entity.Prediction;
import de.mwmrs.entity.ScoringRule;
import de.mwmrs.entity.Team;
import de.mwmrs.security.PasswordService;
import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.jboss.logging.Logger;

/**
 * Seeds a fake "MatchWiz Test Cup 2026" for local development.
 * Creates 4 teams, 2 matchdays, 4 matches, 1 group, 4 approved users and
 * predictions for MD1 (past matches) so the global admin can enter results
 * and observe scoring + ranking.
 *
 * Admin credentials (from AdminSeeder) are used to call PATCH /matches/{id}.
 * Test-user password for alice/bob/carol/dave is "test123".
 *
 * Idempotent: skipped when the competition already exists.
 */
@ApplicationScoped
@IfBuildProfile("dev")
public class TestDataSeeder {

    private static final Logger LOG = Logger.getLogger(TestDataSeeder.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");

    @Inject
    PasswordService passwordService;

    @Transactional
    void onStart(@Observes StartupEvent event) {
        if (Competition.count("name", "MatchWiz Test Cup 2026") > 0) {
            return;
        }
        seed();
        LOG.info("Seeded MatchWiz Test Cup 2026: 4 teams, 2 matchdays, 4 matches, 1 group, 4 users");
    }

    private void seed() {
        Competition comp = competition();
        scoringRule(comp);

        Team ger = findOrCreateTeam("Germany", "GER", "https://flagcdn.com/w80/de.png");
        Team fra = findOrCreateTeam("France",  "FRA", "https://flagcdn.com/w80/fr.png");
        Team esp = findOrCreateTeam("Spain",   "ESP", "https://flagcdn.com/w80/es.png");
        Team bra = findOrCreateTeam("Brazil",  "BRA", "https://flagcdn.com/w80/br.png");

        // MD1: kickoffs in the past — enter results now to test scoring
        Matchday md1 = matchday(comp, 1);
        Match m1 = match(md1, ger, fra, "05/29/2026 15:00");
        Match m2 = match(md1, esp, bra, "05/30/2026 18:00");

        // MD2: kickoffs in the future — predictions still open
        Matchday md2 = matchday(comp, 2);
        match(md2, ger, esp, "06/21/2026 15:00");
        match(md2, fra, bra, "06/22/2026 18:00");

        Group group = group(comp, "Alpha Tipprunde", "Test group for the fake cup");

        AppUser alice = user("alice", "alice@test.local");
        AppUser bob   = user("bob",   "bob@test.local");
        AppUser carol = user("carol", "carol@test.local");
        AppUser dave  = user("dave",  "dave@test.local");

        membership(group, alice, GroupRole.GROUP_ADMIN, true);
        membership(group, bob,   GroupRole.MEMBER,      true);
        membership(group, carol, GroupRole.MEMBER,      true);
        membership(group, dave,  GroupRole.MEMBER,      true);

        // Predictions for MD1 — mix of exact / goal-difference / tendency / miss hits
        // depending on the result the admin enters.
        //   e.g. if admin sets Germany 2-1 France: alice=5pts(exact), dave=3pts(goaldiff), bob/carol=0
        //   e.g. if admin sets Spain 2-1 Brazil:   bob=2pts(tendency), rest=0
        OffsetDateTime predictionTime = utc("05/27/2026 10:00");
        prediction(alice, group, m1, 2, 1, predictionTime);
        prediction(bob,   group, m1, 1, 1, predictionTime);
        prediction(carol, group, m1, 0, 1, predictionTime);
        prediction(dave,  group, m1, 2, 0, predictionTime);

        prediction(alice, group, m2, 1, 2, predictionTime);
        prediction(bob,   group, m2, 3, 1, predictionTime);
        prediction(carol, group, m2, 2, 2, predictionTime);
        prediction(dave,  group, m2, 1, 1, predictionTime);
    }

    // --- helpers ---

    private Competition competition() {
        var comp = new Competition();
        comp.name = "MatchWiz Test Cup 2026";
        comp.season = "2026";
        comp.status = CompetitionStatus.ACTIVE;
        comp.startDate = LocalDate.of(2026, 5, 29);
        comp.endDate   = LocalDate.of(2026, 6, 30);
        comp.persist();
        return comp;
    }

    private void scoringRule(Competition comp) {
        var rule = new ScoringRule();
        rule.competition = comp;
        rule.persist();
    }

    private Team findOrCreateTeam(String name, String shortName, String logoUrl) {
        Team existing = Team.find("shortName", shortName).firstResult();
        if (existing != null) {
            return existing;
        }
        var t = new Team();
        t.name = name;
        t.shortName = shortName;
        t.logoUrl = logoUrl;
        t.persist();
        return t;
    }

    private Matchday matchday(Competition comp, int number) {
        var md = new Matchday();
        md.competition = comp;
        md.number = number;
        md.persist();
        return md;
    }

    private Match match(Matchday matchday, Team home, Team away, String kickoffStr) {
        var m = new Match();
        m.matchday = matchday;
        m.homeTeam = home;
        m.awayTeam = away;
        m.kickoffTime = utc(kickoffStr);
        m.status = MatchStatus.SCHEDULED;
        m.persist();
        return m;
    }

    private Group group(Competition comp, String name, String description) {
        var g = new Group();
        g.competition = comp;
        g.name = name;
        g.description = description;
        g.persist();
        return g;
    }

    private AppUser user(String username, String email) {
        var u = new AppUser();
        u.username = username;
        u.email = email;
        u.passwordHash = passwordService.hash("test123");
        u.active = true;
        u.persist();
        return u;
    }

    private void membership(Group group, AppUser user, GroupRole role, boolean approved) {
        var m = new GroupMembership();
        m.group = group;
        m.user = user;
        m.role = role;
        m.approved = approved;
        m.persist();
    }

    private void prediction(AppUser user, Group group, Match match,
                            int home, int away, OffsetDateTime submittedAt) {
        var p = new Prediction();
        p.user = user;
        p.group = group;
        p.match = match;
        p.predictedHomeGoals = home;
        p.predictedAwayGoals = away;
        p.submittedAt = submittedAt;
        p.persist();
    }

    private OffsetDateTime utc(String s) {
        return OffsetDateTime.of(LocalDateTime.parse(s, DATE_FMT), ZoneOffset.UTC);
    }
}
