package de.mwmrs.service;

import de.mwmrs.dto.CreateMatchRequest;
import de.mwmrs.dto.UpdateMatchRequest;
import de.mwmrs.entity.Match;
import de.mwmrs.entity.Matchday;
import de.mwmrs.entity.ScoringRule;
import de.mwmrs.entity.Team;
import de.mwmrs.exception.BusinessException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class MatchService {

    @Inject
    ScoringService scoringService;

    public List<Match> listByMatchday(Long matchdayId) {
        return Match.list("matchday.id", matchdayId);
    }

    public Match get(Long id) {
        Match m = Match.findById(id);
        if (m == null) {
            throw BusinessException.notFound("Match not found");
        }
        return m;
    }

    @Transactional
    public Match create(Long matchdayId, CreateMatchRequest req) {
        Matchday matchday = Matchday.findById(matchdayId);
        if (matchday == null) {
            throw BusinessException.notFound("Matchday not found");
        }
        Match m = new Match();
        m.matchday = matchday;
        m.homeTeam = requireTeam(req.homeTeamId());
        m.awayTeam = requireTeam(req.awayTeamId());
        m.kickoffTime = req.kickoffTime();
        m.persist();
        return m;
    }

    /**
     * Updates a match. Setting status to FINISHED with a result triggers
     * (re)scoring of all predictions for the match.
     */
    @Transactional
    public Match update(Long id, UpdateMatchRequest req) {
        Match m = get(id);
        if (req.homeTeamId() != null) {
            m.homeTeam = requireTeam(req.homeTeamId());
        }
        if (req.awayTeamId() != null) {
            m.awayTeam = requireTeam(req.awayTeamId());
        }
        if (req.kickoffTime() != null) {
            m.kickoffTime = req.kickoffTime();
        }
        if (req.homeGoals() != null) {
            m.homeGoals = req.homeGoals();
        }
        if (req.awayGoals() != null) {
            m.awayGoals = req.awayGoals();
        }
        if (req.status() != null) {
            m.status = req.status();
        }

        ScoringRule rule = ScoringRule.findByCompetition(m.matchday.competition.id);
        scoringService.rescore(m, rule);
        return m;
    }

    private Team requireTeam(Long teamId) {
        Team t = Team.findById(teamId);
        if (t == null) {
            throw BusinessException.badRequest("Team not found: " + teamId);
        }
        return t;
    }
}
