package de.mwmrs.matchwiz.resource;

import de.mwmrs.matchwiz.dto.ManualBonusDto;
import de.mwmrs.matchwiz.dto.ManualBonusRequest;
import de.mwmrs.matchwiz.security.CurrentUser;
import de.mwmrs.matchwiz.service.ManualBonusService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/groups/{groupId}/manual-bonuses")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ManualBonusResource {

    @Inject
    ManualBonusService service;

    @Inject
    CurrentUser currentUser;

    @POST
    public Response award(@PathParam("groupId") Long groupId, @Valid ManualBonusRequest request) {
        ManualBonusDto dto = ManualBonusDto.from(
                service.award(groupId, request.userId(), request.points(), request.reason(),
                        currentUser.require()));
        return Response.status(Response.Status.CREATED).entity(dto).build();
    }

    @GET
    public List<ManualBonusDto> list(@PathParam("groupId") Long groupId) {
        return service.listForGroup(groupId, currentUser.require())
                .stream().map(ManualBonusDto::from).toList();
    }

    @DELETE
    @Path("/{bonusId}")
    public Response remove(@PathParam("groupId") Long groupId, @PathParam("bonusId") Long bonusId) {
        service.remove(bonusId, currentUser.require());
        return Response.noContent().build();
    }
}
