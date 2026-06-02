package de.mwmrs.resource;

import de.mwmrs.dto.CreateInvitationRequest;
import de.mwmrs.dto.InvitationDto;
import de.mwmrs.security.GroupAuthz;
import de.mwmrs.service.InvitationService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/groups/{id}/invitations")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GroupInvitationResource {

    @Inject
    InvitationService service;

    @Inject
    GroupAuthz groupAuthz;

    @POST
    public Response createInvitation(@PathParam("id") Long groupId, @Valid CreateInvitationRequest request) {
        groupAuthz.requireGroupAdmin(groupId);
        return Response.status(Response.Status.CREATED)
                .entity(InvitationDto.from(service.create(groupId, request)))
                .build();
    }
}
