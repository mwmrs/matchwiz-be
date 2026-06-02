package de.mwmrs.service;

import de.mwmrs.dto.CreateTeamRequest;
import de.mwmrs.entity.Team;
import de.mwmrs.exception.BusinessException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class TeamService {

    public List<Team> list() {
        return Team.listAll();
    }

    public Team get(Long id) {
        Team t = Team.findById(id);
        if (t == null) {
            throw BusinessException.notFound("Team not found");
        }
        return t;
    }

    @Transactional
    public Team create(CreateTeamRequest req) {
        Team t = new Team();
        t.name = req.name();
        t.shortName = req.shortName();
        t.logoUrl = req.logoUrl();
        t.persist();
        return t;
    }

    @Transactional
    public Team update(Long id, CreateTeamRequest req) {
        Team t = get(id);
        t.name = req.name();
        t.shortName = req.shortName();
        t.logoUrl = req.logoUrl();
        return t;
    }

    @Transactional
    public void delete(Long id) {
        Team t = get(id);
        t.delete();
    }
}
