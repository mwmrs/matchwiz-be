package de.mwmrs.matchwiz.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import java.util.stream.Collectors;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

/**
 * Maps domain and validation exceptions to JSON error responses.
 */
public class ExceptionMappers {

    public record ErrorResponse(int status, String message) {
    }

    @ServerExceptionMapper
    public Response mapBusiness(BusinessException e) {
        return Response.status(e.getStatus())
                .entity(new ErrorResponse(e.getStatus().getStatusCode(), e.getMessage()))
                .build();
    }

    @ServerExceptionMapper
    public Response mapValidation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .collect(Collectors.joining("; "));
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse(400, message))
                .build();
    }
}
