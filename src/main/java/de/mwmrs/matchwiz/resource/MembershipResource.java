package de.mwmrs.matchwiz.resource;

import de.mwmrs.matchwiz.dto.GroupMembershipDto;
import de.mwmrs.matchwiz.security.GroupAuthz;
import de.mwmrs.matchwiz.service.MembershipService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/groups/{id}/members")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
public class MembershipResource {

    @Inject
    MembershipService service;

    @Inject
    GroupAuthz groupAuthz;

    @GET
    public List<GroupMembershipDto> listMembers(@PathParam("id") Long groupId) {
        groupAuthz.requireGroupAdmin(groupId);
        return service.listMembers(groupId);
    }

    @DELETE
    @Path("/{userId}")
    public Response removeMember(@PathParam("id") Long groupId, @PathParam("userId") Long userId) {
        groupAuthz.requireGroupAdmin(groupId);
        service.remove(groupId, userId);
        return Response.noContent().build();
    }

    @POST
    @Path("/{userId}/approve")
    public GroupMembershipDto approveMember(@PathParam("id") Long groupId, @PathParam("userId") Long userId) {
        groupAuthz.requireGroupAdmin(groupId);
        return GroupMembershipDto.from(service.approve(groupId, userId));
    }
}
