package de.mwmrs.resource;

import de.mwmrs.dto.RankingEntryDto;
import de.mwmrs.security.GroupAuthz;
import de.mwmrs.service.RankingService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/groups/{id}/rankings")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
public class RankingResource {

    @Inject
    RankingService service;

    @Inject
    GroupAuthz groupAuthz;

    @GET
    public List<RankingEntryDto> getGroupRankings(@PathParam("id") Long groupId) {
        groupAuthz.requireMember(groupId);
        return service.ranking(groupId);
    }
}
