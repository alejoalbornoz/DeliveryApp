package org.deliveryapp.api_gateway.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

/**
 * Decides whether an incoming request needs JWT validation.
 *
 * Public endpoints (login, register, refresh, swagger, actuator health)
 * are excluded here so the AuthenticationFilter knows to let them pass
 * through without a token. Everything not in this list is treated as
 * secured by default — a deliberately "deny by default" approach.
 */
@Component
public class RouteValidator {

    public static final List<String> OPEN_API_ENDPOINTS = List.of(
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/payments/webhook",   // ← MercadoPago calls this without JWT
            "/api/v1/payments/success",
            "/api/v1/payments/failure",
            "/api/v1/payments/pending",
            "/v3/api-docs",
            "/swagger-ui",
            "/actuator/health"
    );

    public Predicate<ServerHttpRequest> isSecured = request ->
            OPEN_API_ENDPOINTS
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));
}
