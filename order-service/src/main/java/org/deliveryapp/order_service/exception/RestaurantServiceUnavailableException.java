package org.deliveryapp.order_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown by RestaurantClient's fallback method when restaurant-service
 * is down, slow, or the circuit breaker is currently OPEN.
 * Mapped to 503 so the client knows to retry later, as opposed to a
 * 400/404 which would imply the request itself was wrong.
 */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class RestaurantServiceUnavailableException extends RuntimeException {

    public RestaurantServiceUnavailableException() {
        super("Restaurant service is currently unavailable. Please try again shortly.");
    }
}
