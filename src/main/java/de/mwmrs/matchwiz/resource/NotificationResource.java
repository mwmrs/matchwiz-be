package de.mwmrs.matchwiz.resource;

import de.mwmrs.matchwiz.dto.NotificationDto;
import de.mwmrs.matchwiz.security.CurrentUser;
import de.mwmrs.matchwiz.service.NotificationService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/notifications")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
public class NotificationResource {

    @Inject
    NotificationService service;

    @Inject
    CurrentUser currentUser;

    @GET
    public List<NotificationDto> list() {
        return service.listForUser(currentUser.id()).stream().map(NotificationDto::from).toList();
    }

    @PATCH
    @Path("/{id}/read")
    public NotificationDto markRead(@PathParam("id") Long id) {
        return NotificationDto.from(service.markRead(id, currentUser.id()));
    }
}
