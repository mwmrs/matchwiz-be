package de.mwmrs.resource;

import de.mwmrs.dto.LoginRequest;
import de.mwmrs.dto.LoginResponse;
import de.mwmrs.dto.RegisterRequest;
import de.mwmrs.dto.UserDto;
import de.mwmrs.entity.AppUser;
import de.mwmrs.service.AuthService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
public class AuthResource {

    @Inject
    AuthService authService;

    @POST
    @Path("/login")
    public LoginResponse login(@Valid LoginRequest request) {
        return authService.login(request);
    }

    @POST
    @Path("/register")
    public Response register(@Valid RegisterRequest request) {
        AppUser user = authService.register(request);
        return Response.status(Response.Status.CREATED).entity(UserDto.from(user)).build();
    }
}
