package de.mwmrs.matchwiz.resource;

import de.mwmrs.matchwiz.dto.LoginRequest;
import de.mwmrs.matchwiz.dto.LoginResponse;
import de.mwmrs.matchwiz.dto.PasswordResetConfirmRequest;
import de.mwmrs.matchwiz.dto.PasswordResetRequest;
import de.mwmrs.matchwiz.dto.RegisterRequest;
import de.mwmrs.matchwiz.dto.UserDto;
import de.mwmrs.matchwiz.dto.VerifyEmailRequest;
import de.mwmrs.matchwiz.entity.AppUser;
import de.mwmrs.matchwiz.security.CurrentUser;
import de.mwmrs.matchwiz.service.AuthService;
import de.mwmrs.matchwiz.service.EmailVerificationService;
import de.mwmrs.matchwiz.service.PasswordResetService;
import io.quarkus.security.Authenticated;
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
public class AuthResource {

    @Inject
    AuthService authService;

    @Inject
    PasswordResetService passwordResetService;

    @Inject
    EmailVerificationService emailVerificationService;

    @Inject
    CurrentUser currentUser;

    @POST
    @Path("/login")
    @PermitAll
    public LoginResponse login(@Valid LoginRequest request) {
        return authService.login(request);
    }

    @POST
    @Path("/register")
    @PermitAll
    public Response register(@Valid RegisterRequest request) {
        AppUser user = authService.register(request);
        return Response.status(Response.Status.CREATED).entity(UserDto.from(user)).build();
    }

    /** Always returns 204 so the endpoint does not leak which emails have an account. */
    @POST
    @Path("/password-reset/request")
    @PermitAll
    public Response requestPasswordReset(@Valid PasswordResetRequest request) {
        passwordResetService.requestReset(request.email());
        return Response.noContent().build();
    }

    @POST
    @Path("/password-reset/confirm")
    @PermitAll
    public Response confirmPasswordReset(@Valid PasswordResetConfirmRequest request) {
        passwordResetService.confirmReset(request.code(), request.newPassword());
        return Response.noContent().build();
    }

    @POST
    @Path("/verify-email/request")
    @Authenticated
    public Response requestEmailVerification() {
        emailVerificationService.requestVerification(currentUser.require());
        return Response.noContent().build();
    }

    @POST
    @Path("/verify-email/confirm")
    @Authenticated
    public Response confirmEmailVerification(@Valid VerifyEmailRequest request) {
        emailVerificationService.confirmVerification(currentUser.require(), request.code());
        return Response.noContent().build();
    }
}
