package de.mwmrs.service;

import de.mwmrs.dto.CreateCompetitionRequest;
import de.mwmrs.dto.ScoringRuleDto;
import de.mwmrs.dto.UpdateCompetitionRequest;
import de.mwmrs.entity.Competition;
import de.mwmrs.entity.ScoringRule;
import de.mwmrs.exception.BusinessException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class CompetitionService {

    public List<Competition> list() {
        return Competition.listAll();
    }

    public Competition get(Long id) {
        Competition c = Competition.findById(id);
        if (c == null) {
            throw BusinessException.notFound("Competition not found");
        }
        return c;
    }

    /** Creates a competition together with its default scoring rule (5/3/2). */
    @Transactional
    public Competition create(CreateCompetitionRequest req) {
        Competition c = new Competition();
        c.name = req.name();
        c.season = req.season();
        c.startDate = req.startDate();
        c.endDate = req.endDate();
        c.persist();

        ScoringRule rule = new ScoringRule();
        rule.competition = c;
        rule.persist();
        return c;
    }

    @Transactional
    public Competition update(Long id, UpdateCompetitionRequest req) {
        Competition c = get(id);
        if (req.name() != null) {
            c.name = req.name();
        }
        if (req.season() != null) {
            c.season = req.season();
        }
        if (req.status() != null) {
            c.status = req.status();
        }
        if (req.startDate() != null) {
            c.startDate = req.startDate();
        }
        if (req.endDate() != null) {
            c.endDate = req.endDate();
        }
        return c;
    }

    public ScoringRule getScoringRule(Long competitionId) {
        get(competitionId);
        ScoringRule rule = ScoringRule.findByCompetition(competitionId);
        if (rule == null) {
            throw BusinessException.notFound("Scoring rule not found");
        }
        return rule;
    }

    @Transactional
    public ScoringRule updateScoringRule(Long competitionId, ScoringRuleDto dto) {
        ScoringRule rule = getScoringRule(competitionId);
        rule.exactResultPoints = dto.exactResultPoints();
        rule.goalDifferencePoints = dto.goalDifferencePoints();
        rule.tendencyPoints = dto.tendencyPoints();
        return rule;
    }
}
