package de.mwmrs.resource;

import de.mwmrs.dto.UpdateUserRequest;
import de.mwmrs.dto.UserDto;
import de.mwmrs.entity.AppUser;
import de.mwmrs.security.CurrentUser;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/users/me")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    CurrentUser currentUser;

    @GET
    public UserDto getMe() {
        return UserDto.from(currentUser.require());
    }

    @PATCH
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
