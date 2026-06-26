package de.mwmrs.matchwiz.security;

import de.mwmrs.matchwiz.exception.ExceptionMappers.ErrorResponse;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class RateLimitFilter {

    @ConfigProperty(name = "matchwiz.rate-limit.enabled", defaultValue = "true")
    boolean enabled;

    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> resetBuckets = new ConcurrentHashMap<>();

    @ServerRequestFilter
    public Optional<RestResponse<ErrorResponse>> filter(ContainerRequestContext ctx,
                                                        RoutingContext rc) {
        if (!enabled) {
            return Optional.empty();
        }
        String path = ctx.getUriInfo().getPath();
        String ip = clientIp(rc);

        Bucket bucket = null;
        if (path.endsWith("/auth/login")) {
            bucket = loginBuckets.computeIfAbsent(ip, k -> loginBucket());
        } else if (path.contains("/auth/password-reset/")) {
            bucket = resetBuckets.computeIfAbsent(ip, k -> resetBucket());
        }

        if (bucket != null && !bucket.tryConsume(1)) {
            return Optional.of(RestResponse.status(
                    Response.Status.TOO_MANY_REQUESTS,
                    new ErrorResponse(429, "Too many requests, please try again later.")));
        }
        return Optional.empty();
    }

    // 10 attempts per minute per IP
    private Bucket loginBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(10)
                        .refillGreedy(10, Duration.ofMinutes(1))
                        .build())
                .build();
    }

    // 5 attempts per 15 minutes per IP
    private Bucket resetBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(5)
                        .refillGreedy(5, Duration.ofMinutes(15))
                        .build())
                .build();
    }

    // Respects X-Forwarded-For (Cloudflare / reverse proxy), falls back to direct IP
    private String clientIp(RoutingContext rc) {
        String forwarded = rc.request().getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return rc.request().remoteAddress().hostAddress();
    }
}
