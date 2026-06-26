package de.mwmrs.matchwiz.service;

import de.mwmrs.matchwiz.dto.CreateTeamRequest;
import de.mwmrs.matchwiz.entity.Team;
import de.mwmrs.matchwiz.exception.BusinessException;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class TeamService {

    public List<Team> list() {
        return Team.listAll(Sort.ascending("name"));
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
