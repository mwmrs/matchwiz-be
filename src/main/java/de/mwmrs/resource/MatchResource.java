package de.mwmrs.resource;

import de.mwmrs.dto.MatchDto;
import de.mwmrs.dto.UpdateMatchRequest;
import de.mwmrs.service.MatchService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/matches")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MatchResource {

    @Inject
    MatchService service;

    @PATCH
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public MatchDto update(@PathParam("id") Long id, @Valid UpdateMatchRequest request) {
        return MatchDto.from(service.update(id, request));
    }
}
