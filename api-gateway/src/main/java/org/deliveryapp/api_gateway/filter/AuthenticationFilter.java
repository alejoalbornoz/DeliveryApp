package org.deliveryapp.filter;



import org.deliveryapp.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Custom Gateway filter that runs BEFORE a request is forwarded to any
 * downstream microservice.
 *
 * Flow for a secured route:
 *   1. RouteValidator says this path needs auth.
 *   2. Look for an "Authorization: Bearer <token>" header. Missing -> 401.
 *   3. Validate the token with JwtUtil. Invalid/expired -> 401.
 *   4. Inject the username as a custom header (X-Auth-User) so
 *      downstream services can trust "this request was already
 *      authenticated by the gateway" without re-validating the JWT
 *      themselves.
 *   5. Let the request continue down the filter chain (chain.filter).
 *
 * Public routes (RouteValidator.isSecured == false) skip all of this
 * and go straight to chain.filter(exchange).
 */
@Slf4j
@Component
public class AuthenticationFilter
        extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final JwtUtil jwtUtil;
    private final RouteValidator routeValidator;

    public AuthenticationFilter(JwtUtil jwtUtil, RouteValidator routeValidator) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
        this.routeValidator = routeValidator;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            if (!routeValidator.isSecured.test(exchange.getRequest())) {
                // Public route (login, register, swagger, health) -> let it pass
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or malformed Authorization header for secured route: {}",
                        exchange.getRequest().getURI().getPath());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authHeader.substring(7); // strip "Bearer "

            if (!jwtUtil.isTokenValid(token)) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            // Forward the resolved username downstream as a trusted header.
            // Downstream services read X-Auth-User instead of re-parsing the JWT.
            String username = jwtUtil.extractUsername(token);
            var mutatedExchange = exchange.mutate()
                    .request(r -> r.header("X-Auth-User", username))
                    .build();

            return chain.filter(mutatedExchange);
        };
    }

    public static class Config {
        // Intentionally empty: this filter needs no per-route configuration,
        // but AbstractGatewayFilterFactory requires a Config class to exist.
    }
}