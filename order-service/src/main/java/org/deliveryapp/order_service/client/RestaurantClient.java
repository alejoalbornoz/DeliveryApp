package org.deliveryapp.order_service.client;


import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.deliveryapp.order_service.dto.response.MenuItemResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for calling restaurant-service.
 *
 * "name" must match restaurant-service's spring.application.name —
 * that's the key Eureka uses to resolve the real host:port at runtime
 * (same "lb://" load-balancing mechanism used by the Gateway).
 *
 * @CircuitBreaker wraps every call: after enough failures within the
 * configured sliding window, the circuit "opens" and immediately
 * routes to the fallback method below instead of waiting on a slow
 * or dead restaurant-service.
 *
 * @Retry runs BEFORE the circuit breaker sees a final failure: a
 * single transient network blip gets retried automatically without
 * ever counting against the circuit breaker's failure rate.
 */
@FeignClient(name = "restaurant-service", path = "/api/v1")
public interface RestaurantClient {

    @GetMapping("/menu/{menuItemId}")
    @CircuitBreaker(name = "restaurantService", fallbackMethod = "getMenuItemFallback")
    @Retry(name = "restaurantService")
    MenuItemResponseDTO getMenuItemById(@PathVariable Long menuItemId);

    /**
     * Fallback signature must match the original method PLUS a final
     * Throwable parameter. Returning null here is intentional: the
     * calling service layer checks for null and throws a domain-specific
     * MenuItemUnavailableException, which is more meaningful to the
     * client than letting a raw Feign/circuit-breaker exception bubble up.
     */
    default MenuItemResponseDTO getMenuItemFallback(Long menuItemId, Throwable ex) {
        return null;
    }
}
