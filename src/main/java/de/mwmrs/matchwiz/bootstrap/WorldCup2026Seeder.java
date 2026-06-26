package de.mwmrs.matchwiz.bootstrap;

import de.mwmrs.matchwiz.entity.Competition;
import de.mwmrs.matchwiz.entity.CompetitionStatus;
import de.mwmrs.matchwiz.entity.Match;
import de.mwmrs.matchwiz.entity.MatchStatus;
import de.mwmrs.matchwiz.entity.Matchday;
import de.mwmrs.matchwiz.entity.ScoringRule;
import de.mwmrs.matchwiz.entity.Team;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.jboss.logging.Logger;

/**
 * Seeds FIFA World Cup 2026 reference data (48 teams, 3 group-stage matchdays,
 * 72 group matches). Knockout matches are omitted because team assignments are
 * not yet known. Kickoff times are stored as UTC; the API provides local times
 * without timezone context across three host countries.
 *
 * Idempotent: skipped when the competition already exists.
 */
@ApplicationScoped
public class WorldCup2026Seeder {

    private static final Logger LOG = Logger.getLogger(WorldCup2026Seeder.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");

    @ConfigProperty(name = "matchwiz.seeders.world-cup-2026.enabled", defaultValue = "false")
    boolean enabled;

    @Transactional
    void onStart(@Observes StartupEvent event) {
        if (!enabled) {
            return;
        }
        if (Competition.count("name", "FIFA World Cup 2026") > 0) {
            return;
        }
        seed();
        LOG.info("Seeded FIFA World Cup 2026: 48 teams, 3 matchdays, 72 group-stage matches");
    }

    private void seed() {
        Competition comp = competition();
        scoringRule(comp);
        var teams = teams();
        matchdays(comp, teams);
    }

    private Competition competition() {
        var comp = new Competition();
        comp.name = "FIFA World Cup 2026";
        comp.season = "2026";
        comp.status = CompetitionStatus.DRAFT;
        comp.startDate = LocalDate.of(2026, 6, 11);
        comp.endDate = LocalDate.of(2026, 7, 19);
        comp.persist();
        return comp;
    }

    private void scoringRule(Competition comp) {
        var rule = new ScoringRule();
        rule.competition = comp;
        rule.persist();
    }

    private Map<String, Team> teams() {
        var map = new HashMap<String, Team>();
        map.put("1",  team("Mexico",                          "MEX", "https://flagcdn.com/w80/mx.png"));
        map.put("2",  team("South Africa",                    "RSA", "https://flagcdn.com/w80/za.png"));
        map.put("3",  team("South Korea",                     "KOR", "https://flagcdn.com/w80/kr.png"));
        map.put("4",  team("Czech Republic",                  "CZE", "https://flagcdn.com/w80/cz.png"));
        map.put("5",  team("Canada",                          "CAN", "https://flagcdn.com/w80/ca.png"));
        map.put("6",  team("Bosnia and Herzegovina",          "BIH", "https://flagcdn.com/w80/ba.png"));
        map.put("7",  team("Qatar",                           "QAT", "https://flagcdn.com/w80/qa.png"));
        map.put("8",  team("Switzerland",                     "SUI", "https://flagcdn.com/w80/ch.png"));
        map.put("9",  team("Brazil",                          "BRA", "https://flagcdn.com/w80/br.png"));
        map.put("10", team("Morocco",                         "MAR", "https://flagcdn.com/w80/ma.png"));
        map.put("11", team("Haiti",                           "HAI", "https://flagcdn.com/w80/ht.png"));
        map.put("12", team("Scotland",                        "SCO", "https://flagcdn.com/w80/gb-sct.png"));
        map.put("13", team("United States",                   "USA", "https://flagcdn.com/w80/us.png"));
        map.put("14", team("Paraguay",                        "PAR", "https://flagcdn.com/w80/py.png"));
        map.put("15", team("Australia",                       "AUS", "https://flagcdn.com/w80/au.png"));
        map.put("16", team("Turkey",                          "TUR", "https://flagcdn.com/w80/tr.png"));
        map.put("17", team("Germany",                         "GER", "https://flagcdn.com/w80/de.png"));
        map.put("18", team("Curaçao",                    "CUW", "https://flagcdn.com/w80/cw.png"));
        map.put("19", team("Ivory Coast",                     "CIV", "https://flagcdn.com/w80/ci.png"));
        map.put("20", team("Ecuador",                         "ECU", "https://flagcdn.com/w80/ec.png"));
        map.put("21", team("Netherlands",                     "NED", "https://flagcdn.com/w80/nl.png"));
        map.put("22", team("Japan",                           "JPN", "https://flagcdn.com/w80/jp.png"));
        map.put("23", team("Sweden",                          "SWE", "https://flagcdn.com/w80/se.png"));
        map.put("24", team("Tunisia",                         "TUN", "https://flagcdn.com/w80/tn.png"));
        map.put("25", team("Belgium",                         "BEL", "https://flagcdn.com/w80/be.png"));
        map.put("26", team("Egypt",                           "EGY", "https://flagcdn.com/w80/eg.png"));
        map.put("27", team("Iran",                            "IRN", "https://flagcdn.com/w80/ir.png"));
        map.put("28", team("New Zealand",                     "NZL", "https://flagcdn.com/w80/nz.png"));
        map.put("29", team("Spain",                           "ESP", "https://flagcdn.com/w80/es.png"));
        map.put("30", team("Cape Verde",                      "CPV", "https://flagcdn.com/w80/cv.png"));
        map.put("31", team("Saudi Arabia",                    "KSA", "https://flagcdn.com/w80/sa.png"));
        map.put("32", team("Uruguay",                         "URU", "https://flagcdn.com/w80/uy.png"));
        map.put("33", team("France",                          "FRA", "https://flagcdn.com/w80/fr.png"));
        map.put("34", team("Senegal",                         "SEN", "https://flagcdn.com/w80/sn.png"));
        map.put("35", team("Iraq",                            "IRQ", "https://flagcdn.com/w80/iq.png"));
        map.put("36", team("Norway",                          "NOR", "https://flagcdn.com/w80/no.png"));
        map.put("37", team("Argentina",                       "ARG", "https://flagcdn.com/w80/ar.png"));
        map.put("38", team("Algeria",                         "ALG", "https://flagcdn.com/w80/dz.png"));
        map.put("39", team("Austria",                         "AUT", "https://flagcdn.com/w80/at.png"));
        map.put("40", team("Jordan",                          "JOR", "https://flagcdn.com/w80/jo.png"));
        map.put("41", team("Portugal",                        "POR", "https://flagcdn.com/w80/pt.png"));
        map.put("42", team("Democratic Republic of the Congo","COD", "https://flagcdn.com/w80/cd.png"));
        map.put("43", team("Uzbekistan",                      "UZB", "https://flagcdn.com/w80/uz.png"));
        map.put("44", team("Colombia",                        "COL", "https://flagcdn.com/w80/co.png"));
        map.put("45", team("England",                         "ENG", "https://flagcdn.com/w80/gb-eng.png"));
        map.put("46", team("Croatia",                         "CRO", "https://flagcdn.com/w80/hr.png"));
        map.put("47", team("Ghana",                           "GHA", "https://flagcdn.com/w80/gh.png"));
        map.put("48", team("Panama",                          "PAN", "https://flagcdn.com/w80/pa.png"));
        return map;
    }

    private void matchdays(Competition comp, Map<String, Team> t) {
        var md1 = matchday(comp, 1);
        var md2 = matchday(comp, 2);
        var md3 = matchday(comp, 3);

        // Matchday 1
        match(md1, t.get("1"),  t.get("2"),  "06/11/2026 13:00");
        match(md1, t.get("3"),  t.get("4"),  "06/11/2026 20:00");
        match(md1, t.get("5"),  t.get("6"),  "06/12/2026 15:00");
        match(md1, t.get("13"), t.get("14"), "06/12/2026 18:00");
        match(md1, t.get("11"), t.get("12"), "06/13/2026 21:00");
        match(md1, t.get("15"), t.get("16"), "06/13/2026 21:00");
        match(md1, t.get("9"),  t.get("10"), "06/13/2026 18:00");
        match(md1, t.get("7"),  t.get("8"),  "06/13/2026 12:00");
        match(md1, t.get("19"), t.get("20"), "06/14/2026 19:00");
        match(md1, t.get("17"), t.get("18"), "06/14/2026 12:00");
        match(md1, t.get("21"), t.get("22"), "06/14/2026 15:00");
        match(md1, t.get("23"), t.get("24"), "06/14/2026 20:00");
        match(md1, t.get("27"), t.get("28"), "06/15/2026 18:00");
        match(md1, t.get("29"), t.get("30"), "06/15/2026 12:00");
        match(md1, t.get("25"), t.get("26"), "06/15/2026 12:00");
        match(md1, t.get("31"), t.get("32"), "06/15/2026 18:00");
        match(md1, t.get("33"), t.get("34"), "06/16/2026 15:00");
        match(md1, t.get("35"), t.get("36"), "06/16/2026 18:00");
        match(md1, t.get("37"), t.get("38"), "06/16/2026 20:00");
        match(md1, t.get("39"), t.get("40"), "06/16/2026 21:00");
        match(md1, t.get("41"), t.get("42"), "06/17/2026 12:00");
        match(md1, t.get("45"), t.get("46"), "06/17/2026 15:00");
        match(md1, t.get("43"), t.get("44"), "06/17/2026 20:00");
        match(md1, t.get("47"), t.get("48"), "06/17/2026 19:00");

        // Matchday 2
        match(md2, t.get("1"),  t.get("3"),  "06/18/2026 19:00");
        match(md2, t.get("8"),  t.get("6"),  "06/18/2026 12:00");
        match(md2, t.get("5"),  t.get("7"),  "06/18/2026 15:00");
        match(md2, t.get("4"),  t.get("2"),  "06/18/2026 12:00");
        match(md2, t.get("9"),  t.get("11"), "06/19/2026 21:00");
        match(md2, t.get("12"), t.get("10"), "06/19/2026 18:00");
        match(md2, t.get("13"), t.get("15"), "06/19/2026 12:00");
        match(md2, t.get("16"), t.get("14"), "06/19/2026 20:00");
        match(md2, t.get("17"), t.get("19"), "06/20/2026 16:00");
        match(md2, t.get("20"), t.get("18"), "06/20/2026 19:00");
        match(md2, t.get("21"), t.get("23"), "06/20/2026 12:00");
        match(md2, t.get("24"), t.get("22"), "06/20/2026 22:00");
        match(md2, t.get("25"), t.get("27"), "06/21/2026 12:00");
        match(md2, t.get("28"), t.get("26"), "06/21/2026 18:00");
        match(md2, t.get("29"), t.get("31"), "06/21/2026 12:00");
        match(md2, t.get("32"), t.get("30"), "06/21/2026 18:00");
        match(md2, t.get("33"), t.get("35"), "06/22/2026 17:00");
        match(md2, t.get("36"), t.get("34"), "06/22/2026 20:00");
        match(md2, t.get("37"), t.get("39"), "06/22/2026 12:00");
        match(md2, t.get("40"), t.get("38"), "06/22/2026 20:00");
        match(md2, t.get("41"), t.get("43"), "06/23/2026 12:00");
        match(md2, t.get("48"), t.get("46"), "06/23/2026 19:00");
        match(md2, t.get("44"), t.get("42"), "06/23/2026 20:00");
        match(md2, t.get("45"), t.get("47"), "06/23/2026 16:00");

        // Matchday 3
        match(md3, t.get("12"), t.get("9"),  "06/24/2026 18:00");
        match(md3, t.get("10"), t.get("11"), "06/24/2026 18:00");
        match(md3, t.get("2"),  t.get("3"),  "06/24/2026 19:00");
        match(md3, t.get("4"),  t.get("1"),  "06/24/2026 19:00");
        match(md3, t.get("6"),  t.get("7"),  "06/24/2026 12:00");
        match(md3, t.get("8"),  t.get("5"),  "06/24/2026 12:00");
        match(md3, t.get("18"), t.get("19"), "06/25/2026 16:00");
        match(md3, t.get("20"), t.get("17"), "06/25/2026 16:00");
        match(md3, t.get("14"), t.get("15"), "06/25/2026 19:00");
        match(md3, t.get("16"), t.get("13"), "06/25/2026 19:00");
        match(md3, t.get("22"), t.get("23"), "06/25/2026 18:00");
        match(md3, t.get("24"), t.get("21"), "06/25/2026 18:00");
        match(md3, t.get("34"), t.get("35"), "06/26/2026 15:00");
        match(md3, t.get("36"), t.get("33"), "06/26/2026 15:00");
        match(md3, t.get("26"), t.get("27"), "06/26/2026 20:00");
        match(md3, t.get("28"), t.get("25"), "06/26/2026 20:00");
        match(md3, t.get("30"), t.get("31"), "06/26/2026 19:00");
        match(md3, t.get("32"), t.get("29"), "06/26/2026 18:00");
        match(md3, t.get("48"), t.get("45"), "06/27/2026 17:00");
        match(md3, t.get("46"), t.get("47"), "06/27/2026 17:00");
        match(md3, t.get("38"), t.get("39"), "06/27/2026 21:00");
        match(md3, t.get("40"), t.get("37"), "06/27/2026 21:00");
        match(md3, t.get("44"), t.get("41"), "06/27/2026 19:30");
        match(md3, t.get("42"), t.get("43"), "06/27/2026 19:30");
    }

    // --- helpers ---

    private Team team(String name, String shortName, String logoUrl) {
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

    private void match(Matchday matchday, Team home, Team away, String kickoffStr) {
        var m = new Match();
        m.matchday = matchday;
        m.homeTeam = home;
        m.awayTeam = away;
        m.kickoffTime = parseUtc(kickoffStr);
        m.status = MatchStatus.SCHEDULED;
        m.persist();
    }

    private OffsetDateTime parseUtc(String s) {
        return OffsetDateTime.of(
                java.time.LocalDateTime.parse(s, DATE_FMT),
                ZoneOffset.UTC);
    }
}
