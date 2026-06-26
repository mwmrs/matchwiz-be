package de.mwmrs.matchwiz.service;

import de.mwmrs.matchwiz.dto.CreateMatchdayRequest;
import de.mwmrs.matchwiz.entity.Competition;
import de.mwmrs.matchwiz.entity.Matchday;
import de.mwmrs.matchwiz.exception.BusinessException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class MatchdayService {

    public List<Matchday> listByCompetition(Long competitionId) {
        return Matchday.list("competition.id = ?1 order by number", competitionId);
    }

    public Matchday get(Long id) {
        Matchday m = Matchday.findById(id);
        if (m == null) {
            throw BusinessException.notFound("Matchday not found");
        }
        return m;
    }

    @Transactional
    public Matchday create(CreateMatchdayRequest req) {
        Competition competition = Competition.findById(req.competitionId());
        if (competition == null) {
            throw BusinessException.badRequest("Competition not found");
        }
        Matchday m = new Matchday();
        m.competition = competition;
        m.number = req.number();
        m.persist();
        return m;
    }

    @Transactional
    public Matchday update(Long id, CreateMatchdayRequest req) {
        Matchday m = get(id);
        if (req.competitionId() != null && !req.competitionId().equals(m.competition.id)) {
            Competition competition = Competition.findById(req.competitionId());
            if (competition == null) {
                throw BusinessException.badRequest("Competition not found");
            }
            m.competition = competition;
        }
        m.number = req.number();
        return m;
    }
}
