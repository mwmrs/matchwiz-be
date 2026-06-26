package de.mwmrs.matchwiz.service;

import de.mwmrs.matchwiz.entity.AppUser;
import de.mwmrs.matchwiz.entity.GroupMembership;
import de.mwmrs.matchwiz.entity.Match;
import de.mwmrs.matchwiz.entity.MatchStatus;
import de.mwmrs.matchwiz.entity.Prediction;
import de.mwmrs.matchwiz.entity.ReminderLog;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class MatchdayReminderService {

    private static final Logger LOG = Logger.getLogger(MatchdayReminderService.class);

    @Inject
    EmailService emailService;

    @ConfigProperty(name = "matchwiz.reminder.cron")
    String cron;

    @Transactional
    @Scheduled(cron = "{matchwiz.reminder.cron}")
    void sendReminders() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime endOfTomorrow = LocalDate.now(ZoneOffset.UTC)
                .plusDays(2)
                .atStartOfDay()
                .atOffset(ZoneOffset.UTC);

        List<Match> upcomingMatches = Match.list(
                "status = ?1 and kickoffTime >= ?2 and kickoffTime < ?3",
                MatchStatus.SCHEDULED, now, endOfTomorrow);

        if (upcomingMatches.isEmpty()) {
            return;
        }

        // Group matches by competition ID
        Map<Long, List<Match>> matchesByCompetition = upcomingMatches.stream()
                .collect(Collectors.groupingBy(m -> m.matchday.competition.id));

        // user → matches that need a reminder
        Map<AppUser, List<Match>> missingByUser = new HashMap<>();

        for (Map.Entry<Long, List<Match>> entry : matchesByCompetition.entrySet()) {
            Long competitionId = entry.getKey();
            List<Match> compMatches = entry.getValue();

            List<GroupMembership> memberships = GroupMembership.list(
                    "group.competition.id = ?1 and approved = true"
                            + " and user.matchdayReminders = true"
                            + " and user.emailNotifications = true"
                            + " and user.email is not null"
                            + " and user.emailVerified = true",
                    competitionId);

            if (memberships.isEmpty()) {
                continue;
            }

            // user → list of group IDs they hold in this competition
            Map<AppUser, List<Long>> userGroups = memberships.stream()
                    .collect(Collectors.groupingBy(
                            gm -> gm.user,
                            Collectors.mapping(gm -> gm.group.id, Collectors.toList())));

            for (Map.Entry<AppUser, List<Long>> userEntry : userGroups.entrySet()) {
                AppUser user = userEntry.getKey();
                List<Long> groupIds = userEntry.getValue();

                for (Match match : compMatches) {
                    if (ReminderLog.existsByUserAndMatch(user.id, match.id)) {
                        continue;
                    }

                    long predicted = Prediction.count(
                            "user.id = ?1 and match.id = ?2 and group.id in ?3",
                            user.id, match.id, groupIds);

                    if (predicted < groupIds.size()) {
                        missingByUser.computeIfAbsent(user, u -> new ArrayList<>()).add(match);
                    }
                }
            }
        }

        for (Map.Entry<AppUser, List<Match>> entry : missingByUser.entrySet()) {
            AppUser user = entry.getKey();
            List<Match> matches = entry.getValue();

            emailService.sendMatchdayReminder(user, matches);

            for (Match match : matches) {
                ReminderLog log = new ReminderLog();
                log.user = user;
                log.match = match;
                log.persist();
            }

            LOG.infof("Sent matchday reminder to %s for %d match(es)", user.username, matches.size());
        }
    }
}
