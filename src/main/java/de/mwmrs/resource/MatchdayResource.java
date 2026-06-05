package de.mwmrs.resource;

import de.mwmrs.dto.CreateMatchdayRequest;
import de.mwmrs.dto.MatchdayDto;
import de.mwmrs.service.MatchdayService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/matchdays")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MatchdayResource {

    @Inject
    MatchdayService service;

    @GET
    public List<MatchdayDto> list(@QueryParam("competitionId") Long competitionId) {
        return service.listByCompetition(competitionId).stream().map(MatchdayDto::from).toList();
    }

    @POST
    @RolesAllowed("ADMIN")
    public Response create(@Valid CreateMatchdayRequest request) {
        return Response.status(Response.Status.CREATED)
                .entity(MatchdayDto.from(service.create(request)))
                .build();
    }

    @GET
    @Path("/{id}")
    public MatchdayDto get(@PathParam("id") Long id) {
        return MatchdayDto.from(service.get(id));
    }

    @PATCH
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public MatchdayDto update(@PathParam("id") Long id, @Valid CreateMatchdayRequest request) {
        return MatchdayDto.from(service.update(id, request));
    }
}
