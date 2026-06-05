package de.mwmrs.resource;

import de.mwmrs.dto.PredictionDto;
import de.mwmrs.dto.SubmitPredictionRequest;
import de.mwmrs.exception.BusinessException;
import de.mwmrs.security.CurrentUser;
import de.mwmrs.service.PredictionService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/matchdays/{id}/predictions")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PredictionResource {

    @Inject
    PredictionService service;

    @Inject
    CurrentUser currentUser;

    @GET
    public List<PredictionDto> list(@PathParam("id") Long matchdayId,
                                    @QueryParam("groupId") Long groupId) {
        if (groupId == null) {
            throw BusinessException.badRequest("groupId query parameter is required");
        }
        return service.listForUser(matchdayId, groupId, currentUser.id()).stream()
                .map(PredictionDto::from).toList();
    }

    @POST
    public List<PredictionDto> submit(@PathParam("id") Long matchdayId,
                                      @QueryParam("groupId") Long groupId,
                                      List<@Valid SubmitPredictionRequest> requests) {
        if (groupId == null) {
            throw BusinessException.badRequest("groupId query parameter is required");
        }
        return service.submit(matchdayId, groupId, currentUser.require(), requests).stream()
                .map(PredictionDto::from).toList();
    }
}
