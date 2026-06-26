package de.mwmrs.matchwiz.resource;

import de.mwmrs.matchwiz.dto.CompetitionDto;
import de.mwmrs.matchwiz.dto.CreateCompetitionRequest;
import de.mwmrs.matchwiz.dto.ScoringRuleDto;
import de.mwmrs.matchwiz.dto.UpdateCompetitionRequest;
import de.mwmrs.matchwiz.service.CompetitionService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/competitions")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CompetitionResource {

    @Inject
    CompetitionService service;

    @GET
    public List<CompetitionDto> list() {
        return service.list().stream().map(CompetitionDto::from).toList();
    }

    @POST
    @RolesAllowed("ADMIN")
    public Response create(@Valid CreateCompetitionRequest request) {
        return Response.status(Response.Status.CREATED)
                .entity(CompetitionDto.from(service.create(request)))
                .build();
    }

    @GET
    @Path("/{id}")
    public CompetitionDto get(@PathParam("id") Long id) {
        return CompetitionDto.from(service.get(id));
    }

    @PATCH
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public CompetitionDto update(@PathParam("id") Long id, @Valid UpdateCompetitionRequest request) {
        return CompetitionDto.from(service.update(id, request));
    }

    @GET
    @Path("/{id}/scoring-rules")
    public ScoringRuleDto getScoringRules(@PathParam("id") Long id) {
        return ScoringRuleDto.from(service.getScoringRule(id));
    }

    @PUT
    @Path("/{id}/scoring-rules")
    @RolesAllowed("ADMIN")
    public ScoringRuleDto updateScoringRules(@PathParam("id") Long id, @Valid ScoringRuleDto request) {
        return ScoringRuleDto.from(service.updateScoringRule(id, request));
    }
}
