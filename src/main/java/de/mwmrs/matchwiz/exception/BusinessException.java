package de.mwmrs.matchwiz.exception;

import jakarta.ws.rs.core.Response;

/**
 * Domain-level error carrying an HTTP status. Mapped to a JSON error body by
 * {@link de.mwmrs.matchwiz.exception.ExceptionMappers}.
 */
public class BusinessException extends RuntimeException {

    private final Response.Status status;

    public BusinessException(Response.Status status, String message) {
        super(message);
        this.status = status;
    }

    public Response.Status getStatus() {
        return status;
    }

    public static BusinessException notFound(String message) {
        return new BusinessException(Response.Status.NOT_FOUND, message);
    }

    public static BusinessException conflict(String message) {
        return new BusinessException(Response.Status.CONFLICT, message);
    }

    public static BusinessException badRequest(String message) {
        return new BusinessException(Response.Status.BAD_REQUEST, message);
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException(Response.Status.FORBIDDEN, message);
    }

    public static BusinessException unauthorized(String message) {
        return new BusinessException(Response.Status.UNAUTHORIZED, message);
    }
}
