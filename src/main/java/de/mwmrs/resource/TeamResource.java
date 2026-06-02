package de.mwmrs.resource;

import de.mwmrs.dto.CreateTeamRequest;
import de.mwmrs.dto.TeamDto;
import de.mwmrs.service.TeamService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/teams")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TeamResource {

    @Inject
    TeamService service;

    @GET
    public List<TeamDto> list() {
        return service.list().stream().map(TeamDto::from).toList();
    }

    @POST
    @RolesAllowed("ADMIN")
    public Response create(@Valid CreateTeamRequest request) {
        return Response.status(Response.Status.CREATED)
                .entity(TeamDto.from(service.create(request)))
                .build();
    }

    @PATCH
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public TeamDto update(@PathParam("id") Long id, @Valid CreateTeamRequest request) {
        return TeamDto.from(service.update(id, request));
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Response delete(@PathParam("id") Long id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
