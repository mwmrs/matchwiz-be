package de.mwmrs.matchwiz.bootstrap;

import de.mwmrs.matchwiz.entity.AppUser;
import de.mwmrs.matchwiz.entity.Competition;
import de.mwmrs.matchwiz.entity.CompetitionStatus;
import de.mwmrs.matchwiz.entity.Group;
import de.mwmrs.matchwiz.entity.GroupMembership;
import de.mwmrs.matchwiz.entity.GroupRole;
import de.mwmrs.matchwiz.entity.Match;
import de.mwmrs.matchwiz.entity.MatchStatus;
import de.mwmrs.matchwiz.entity.Matchday;
import de.mwmrs.matchwiz.entity.Prediction;
import de.mwmrs.matchwiz.entity.ScoringRule;
import de.mwmrs.matchwiz.entity.Team;
import de.mwmrs.matchwiz.security.PasswordService;
import de.mwmrs.matchwiz.service.ScoringService;
import io.quarkus.arc.profile.IfBuildProfile;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.jboss.logging.Logger;

/**
 * Seeds a fake "MatchWiz Test Cup 2026" for local development.
 * Creates 4 teams, 3 matchdays, 6 matches, 1 group, 4 approved users and
 * predictions for all matches with awarded points pre-computed so rankings,
 * per-matchday filters, and prediction visibility can be tested immediately.
 *
 * All dates are relative to the current date so the data stays realistic
 * regardless of when the app is first started:
 *   MD1 (now-41d / now-40d): 2 FINISHED matches
 *   MD2 (now-5d  / now-1d):  2 FINISHED matches
 *   MD3 (now-1h  / now+3d):  1 LIVE match + 1 SCHEDULED
 *
 * Expected overall rankings: bob=15, alice=11, dave=9, carol=6
 * Test-user password for alice/bob/carol/dave is "test123".
 *
 * Idempotent: skipped when the competition already exists.
 */
@ApplicationScoped
@IfBuildProfile("dev")
public class TestDataSeeder {

    private static final Logger LOG = Logger.getLogger(TestDataSeeder.class);

    @ConfigProperty(name = "matchwiz.seeders.test-data.enabled", defaultValue = "false")
    boolean enabled;

    @Inject
    PasswordService passwordService;

    @Inject
    ScoringService scoringService;

    @Transactional
    void onStart(@Observes StartupEvent event) {
        if (!enabled) return;
        if (Competition.count("name", "MatchWiz Test Cup 2026") > 0) {
            return;
        }
        seed();
        LOG.info("Seeded MatchWiz Test Cup 2026: 4 teams, 3 matchdays, 6 matches (4 finished, 1 live, 1 scheduled), 1 group, 4 users");
    }

    private void seed() {
        Competition comp = competition();
        ScoringRule rule = scoringRule(comp);

        Team ger = findOrCreateTeam("Germany", "GER", "https://flagcdn.com/w80/de.png");
        Team fra = findOrCreateTeam("France",  "FRA", "https://flagcdn.com/w80/fr.png");
        Team esp = findOrCreateTeam("Spain",   "ESP", "https://flagcdn.com/w80/es.png");
        Team bra = findOrCreateTeam("Brazil",  "BRA", "https://flagcdn.com/w80/br.png");

        // MD1: FINISHED — results entered, scoring computed
        Matchday md1 = matchday(comp, 1);
        Match m1 = finishedMatch(md1, ger, fra, daysAgo(41, 15), 2, 1);
        Match m2 = finishedMatch(md1, esp, bra, daysAgo(40, 18), 1, 1);

        // MD2: FINISHED — results entered, scoring computed
        Matchday md2 = matchday(comp, 2);
        Match m3 = finishedMatch(md2, ger, esp, daysAgo(5, 15), 0, 2);
        Match m4 = finishedMatch(md2, fra, bra, daysAgo(1, 18), 1, 0);

        // MD3: one LIVE match (partial score), one SCHEDULED (predictions open)
        Matchday md3 = matchday(comp, 3);
        Match m5 = liveMatch(md3, ger, bra, hoursAgo(1), 1, 0);
        Match m6 = scheduledMatch(md3, fra, esp, daysFromNow(3, 18));

        Group group = group(comp, "Alpha Tipprunde", "Test group for the fake cup");

        AppUser alice = user("alice", "alice@test.local");
        AppUser bob   = user("bob",   "bob@test.local");
        AppUser carol = user("carol", "carol@test.local");
        AppUser dave  = user("dave",  "dave@test.local");

        membership(group, alice, GroupRole.GROUP_ADMIN, true);
        membership(group, bob,   GroupRole.MEMBER,      true);
        membership(group, carol, GroupRole.MEMBER,      true);
        membership(group, dave,  GroupRole.MEMBER,      true);

        // MD1 predictions — submitted 3 days before MD1
        // GER 2-1 FRA: alice=exact(5), dave=tendency(2), bob/carol=miss(0)
        // ESP 1-1 BRA: dave=exact(5), carol=goal-diff(3), alice/bob=miss(0)
        // MD1 totals: dave=7, alice=5, carol=3, bob=0
        OffsetDateTime t1 = daysAgo(44, 10);
        prediction(alice, group, m1, 2, 1, t1, rule);
        prediction(bob,   group, m1, 1, 1, t1, rule);
        prediction(carol, group, m1, 0, 1, t1, rule);
        prediction(dave,  group, m1, 2, 0, t1, rule);

        prediction(alice, group, m2, 1, 2, t1, rule);
        prediction(bob,   group, m2, 3, 1, t1, rule);
        prediction(carol, group, m2, 2, 2, t1, rule);
        prediction(dave,  group, m2, 1, 1, t1, rule);

        // MD2 predictions — submitted 3 days before MD2
        // GER 0-2 ESP: bob=exact(5), alice/dave=tendency(2), carol=miss(0)
        // FRA 1-0 BRA: bob=exact(5), carol=goal-diff(3), alice=tendency(2), dave=miss(0)
        // MD2 totals: bob=10, alice=4, carol=3, dave=2
        OffsetDateTime t2 = daysAgo(8, 10);
        prediction(alice, group, m3, 1, 2, t2, rule);
        prediction(bob,   group, m3, 0, 2, t2, rule);
        prediction(carol, group, m3, 1, 1, t2, rule);
        prediction(dave,  group, m3, 0, 3, t2, rule);

        prediction(alice, group, m4, 2, 0, t2, rule);
        prediction(bob,   group, m4, 1, 0, t2, rule);
        prediction(carol, group, m4, 2, 1, t2, rule);
        prediction(dave,  group, m4, 0, 1, t2, rule);

        // MD3 predictions — submitted 2 days before MD3
        // GER 1-0 BRA (LIVE): bob=exact(5), alice=tendency(2), carol/dave=miss(0)
        // FRA vs ESP (SCHEDULED): no result yet, awardedPoints=null
        // MD3 live-only totals: bob=5, alice=2, carol=0, dave=0
        OffsetDateTime t3 = daysAgo(2, 10);
        prediction(alice, group, m5, 2, 0, t3, rule);
        prediction(bob,   group, m5, 1, 0, t3, rule);
        prediction(carol, group, m5, 0, 0, t3, rule);
        prediction(dave,  group, m5, 1, 1, t3, rule);

        prediction(alice, group, m6, 1, 1, t3, null);
        prediction(bob,   group, m6, 2, 1, t3, null);
        prediction(carol, group, m6, 0, 2, t3, null);
        prediction(dave,  group, m6, 1, 0, t3, null);
    }

    // --- helpers ---

    private Competition competition() {
        var comp = new Competition();
        comp.name = "MatchWiz Test Cup 2026";
        comp.season = "2026";
        comp.status = CompetitionStatus.ACTIVE;
        comp.startDate = LocalDate.now(ZoneOffset.UTC).minusDays(42);
        comp.endDate   = LocalDate.now(ZoneOffset.UTC).plusDays(30);
        comp.persist();
        return comp;
    }

    private ScoringRule scoringRule(Competition comp) {
        var rule = new ScoringRule();
        rule.competition = comp;
        rule.persist();
        return rule;
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

    private Match finishedMatch(Matchday matchday, Team home, Team away, OffsetDateTime kickoff,
                                int homeGoals, int awayGoals) {
        var m = new Match();
        m.matchday = matchday;
        m.homeTeam = home;
        m.awayTeam = away;
        m.kickoffTime = kickoff;
        m.homeGoals = homeGoals;
        m.awayGoals = awayGoals;
        m.status = MatchStatus.FINISHED;
        m.persist();
        return m;
    }

    private Match liveMatch(Matchday matchday, Team home, Team away, OffsetDateTime kickoff,
                            int homeGoals, int awayGoals) {
        var m = new Match();
        m.matchday = matchday;
        m.homeTeam = home;
        m.awayTeam = away;
        m.kickoffTime = kickoff;
        m.homeGoals = homeGoals;
        m.awayGoals = awayGoals;
        m.status = MatchStatus.LIVE;
        m.persist();
        return m;
    }

    private Match scheduledMatch(Matchday matchday, Team home, Team away, OffsetDateTime kickoff) {
        var m = new Match();
        m.matchday = matchday;
        m.homeTeam = home;
        m.awayTeam = away;
        m.kickoffTime = kickoff;
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
                            int home, int away, OffsetDateTime submittedAt, ScoringRule rule) {
        var p = new Prediction();
        p.user = user;
        p.group = group;
        p.match = match;
        p.predictedHomeGoals = home;
        p.predictedAwayGoals = away;
        p.submittedAt = submittedAt;
        if (rule != null && match.homeGoals != null && match.awayGoals != null) {
            p.awardedPoints = scoringService.computePoints(rule, home, away, match.homeGoals, match.awayGoals);
        }
        p.persist();
    }

    private OffsetDateTime daysAgo(int days, int hour) {
        return LocalDate.now(ZoneOffset.UTC).minusDays(days).atTime(hour, 0).atOffset(ZoneOffset.UTC);
    }

    private OffsetDateTime daysFromNow(int days, int hour) {
        return LocalDate.now(ZoneOffset.UTC).plusDays(days).atTime(hour, 0).atOffset(ZoneOffset.UTC);
    }

    private OffsetDateTime hoursAgo(int hours) {
        return OffsetDateTime.now(ZoneOffset.UTC).minusHours(hours).withMinute(0).withSecond(0).withNano(0);
    }
}
