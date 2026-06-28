package org.deliveryapp.delivery_service.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.deliveryapp.delivery_service.dto.request.NotificationRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for calling notification-service.
 *
 * Important design decision: unlike RestaurantClient in order-service
 * (where a failed call means the order genuinely cannot be created),
 * a failed notification should NEVER block a delivery from being
 * assigned or marked as delivered. The driver showed up and dropped
 * off the food; whether the "your order arrived" push notification
 * succeeded is a secondary concern.
 *
 * That's why the fallback here silently logs and returns, instead of
 * throwing a domain exception like MenuItemUnavailableException does
 * in order-service. Same Resilience4j machinery, different fallback
 * philosophy based on how critical the downstream call actually is.
 */
@FeignClient(name = "notification-service", path = "/api/v1/notifications")
public interface NotificationClient {

    @PostMapping
    @CircuitBreaker(name = "notificationService", fallbackMethod = "sendNotificationFallback")
    @Retry(name = "notificationService")
    void sendNotification(@RequestBody NotificationRequestDTO request);

    default void sendNotificationFallback(NotificationRequestDTO request, Throwable ex) {
        // Intentionally swallow the failure — notifications are best-effort.
        // In a real production system this would be logged to a monitoring
        // tool and possibly queued for retry via a message broker.
    }
}
