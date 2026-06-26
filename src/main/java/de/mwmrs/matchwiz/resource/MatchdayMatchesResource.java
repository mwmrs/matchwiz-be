package de.mwmrs.matchwiz.resource;

import de.mwmrs.matchwiz.dto.CreateMatchRequest;
import de.mwmrs.matchwiz.dto.MatchDto;
import de.mwmrs.matchwiz.service.MatchService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/matchdays/{id}/matches")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MatchdayMatchesResource {

    @Inject
    MatchService service;

    @GET
    public List<MatchDto> listByMatchday(@PathParam("id") Long matchdayId) {
        return service.listByMatchday(matchdayId).stream().map(MatchDto::from).toList();
    }

    @POST
    @RolesAllowed("ADMIN")
    public Response create(@PathParam("id") Long matchdayId, @Valid CreateMatchRequest request) {
        return Response.status(Response.Status.CREATED)
                .entity(MatchDto.from(service.create(matchdayId, request)))
                .build();
    }
}
