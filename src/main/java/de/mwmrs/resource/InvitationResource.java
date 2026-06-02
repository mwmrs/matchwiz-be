package de.mwmrs.resource;

import de.mwmrs.dto.GroupMembershipDto;
import de.mwmrs.security.CurrentUser;
import de.mwmrs.service.InvitationService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/invitations")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
public class InvitationResource {

    @Inject
    InvitationService service;

    @Inject
    CurrentUser currentUser;

    @PUT
    @Path("/{token}/accept")
    public GroupMembershipDto acceptInvitation(@PathParam("token") String token) {
        return GroupMembershipDto.from(service.accept(token, currentUser.require()));
    }
}
