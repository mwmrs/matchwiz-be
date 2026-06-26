package de.mwmrs.matchwiz.resource;

import de.mwmrs.matchwiz.dto.GroupMembershipDto;
import de.mwmrs.matchwiz.dto.UpdateUserRequest;
import de.mwmrs.matchwiz.dto.UserDto;
import de.mwmrs.matchwiz.entity.AppUser;
import de.mwmrs.matchwiz.security.CurrentUser;
import de.mwmrs.matchwiz.service.MembershipService;
import de.mwmrs.matchwiz.service.UserService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/users")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    CurrentUser currentUser;

    @Inject
    UserService service;

    @Inject
    MembershipService membershipService;

    @GET
    @RolesAllowed("ADMIN")
    public List<UserDto> listUsers() {
        return service.list().stream().map(UserDto::from).toList();
    }

    @POST
    @Path("/{id}/approve")
    @RolesAllowed("ADMIN")
    public UserDto approveUser(@PathParam("id") Long id) {
        return UserDto.from(service.approve(id));
    }

    @GET
    @Path("/me")
    public UserDto getMe() {
        return UserDto.from(currentUser.require());
    }

    @GET
    @Path("/me/memberships")
    public List<GroupMembershipDto> getMyMemberships() {
        return membershipService.listByUser(currentUser.id()).stream()
                .map(GroupMembershipDto::from).toList();
    }

    @PATCH
    @Path("/me")
    @Transactional
    public UserDto updateMe(@Valid UpdateUserRequest request) {
        AppUser u = currentUser.require();
        if (request.email() != null) {
            u.email = request.email();
        }
        if (request.preferredLanguage() != null) {
            u.preferredLanguage = request.preferredLanguage();
        }
        if (request.timezone() != null) {
            u.timezone = request.timezone();
        }
        if (request.theme() != null) {
            u.theme = request.theme();
        }
        if (request.twoFactorEnabled() != null) {
            u.twoFactorEnabled = request.twoFactorEnabled();
        }
        if (request.emailNotifications() != null) {
            u.emailNotifications = request.emailNotifications();
        }
        if (request.matchdayReminders() != null) {
            u.matchdayReminders = request.matchdayReminders();
        }
        return UserDto.from(u);
    }
}
