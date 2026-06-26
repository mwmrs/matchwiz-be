package de.mwmrs.matchwiz.service;

import de.mwmrs.matchwiz.dto.CreateGroupRequest;
import de.mwmrs.matchwiz.entity.Competition;
import de.mwmrs.matchwiz.entity.CompetitionStatus;
import de.mwmrs.matchwiz.entity.Group;
import de.mwmrs.matchwiz.exception.BusinessException;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class GroupService {

    public List<Group> list(Long competitionId) {
        if (competitionId != null) {
            return Group.list("competition.id", competitionId);
        }
        return Group.listAll(Sort.ascending("name"));
    }

    public Group get(Long id) {
        Group g = Group.findById(id);
        if (g == null) {
            throw BusinessException.notFound("Group not found");
        }
        return g;
    }

    @Transactional
    public Group create(CreateGroupRequest req) {
        Competition competition = Competition.findById(req.competitionId());
        if (competition == null) {
            throw BusinessException.badRequest("Competition not found");
        }
        if (competition.status != CompetitionStatus.ACTIVE) {
            throw BusinessException.conflict("Cannot create group: competition is not active");
        }
        Group g = new Group();
        g.competition = competition;
        g.name = req.name();
        g.description = req.description();
        g.persist();
        return g;
    }

    @Transactional
    public Group update(Long id, CreateGroupRequest req) {
        Group g = get(id);
        if (req.competitionId() != null && !req.competitionId().equals(g.competition.id)) {
            Competition competition = Competition.findById(req.competitionId());
            if (competition == null) {
                throw BusinessException.badRequest("Competition not found");
            }
            if (competition.status != CompetitionStatus.ACTIVE) {
                throw BusinessException.conflict("Cannot move group: target competition is not active");
            }
            g.competition = competition;
        }
        g.name = req.name();
        g.description = req.description();
        return g;
    }

    @Transactional
    public void delete(Long id) {
        Group g = get(id);
        g.delete();
    }
}
