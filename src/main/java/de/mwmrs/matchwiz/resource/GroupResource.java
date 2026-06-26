package de.mwmrs.resource;

import de.mwmrs.dto.CreateGroupRequest;
import de.mwmrs.dto.GroupDto;
import de.mwmrs.dto.GroupMembershipDto;
import de.mwmrs.security.CurrentUser;
import de.mwmrs.service.GroupService;
import de.mwmrs.service.MembershipService;
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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/groups")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GroupResource {

    @Inject
    GroupService service;

    @Inject
    MembershipService membershipService;

    @Inject
    CurrentUser currentUser;

    @GET
    public List<GroupDto> list(@QueryParam("competitionId") Long competitionId) {
        return service.list(competitionId).stream().map(GroupDto::from).toList();
    }

    @POST
    @RolesAllowed("ADMIN")
    public Response create(@Valid CreateGroupRequest request) {
        return Response.status(Response.Status.CREATED)
                .entity(GroupDto.from(service.create(request)))
                .build();
    }

    @GET
    @Path("/{id}")
    public GroupDto get(@PathParam("id") Long id) {
        return GroupDto.from(service.get(id));
    }

    @PATCH
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public GroupDto update(@PathParam("id") Long id, @Valid CreateGroupRequest request) {
        return GroupDto.from(service.update(id, request));
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Response delete(@PathParam("id") Long id) {
        service.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/join")
    public Response joinGroup(@PathParam("id") Long id) {
        GroupMembershipDto dto = GroupMembershipDto.from(
                membershipService.join(id, currentUser.require()));
        return Response.status(Response.Status.CREATED).entity(dto).build();
    }
}
