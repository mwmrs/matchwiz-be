package de.mwmrs.resource;

import de.mwmrs.dto.LegalNoticeDto;
import de.mwmrs.service.LegalNoticeService;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/legal-notice")
@PermitAll
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LegalNoticeResource {

    @Inject
    LegalNoticeService service;

    @GET
    public LegalNoticeDto get() {
        return LegalNoticeDto.from(service.get());
    }

    @PUT
    @RolesAllowed("ADMIN")
    public LegalNoticeDto update(@Valid LegalNoticeDto request) {
        return LegalNoticeDto.from(service.update(request));
    }
}
