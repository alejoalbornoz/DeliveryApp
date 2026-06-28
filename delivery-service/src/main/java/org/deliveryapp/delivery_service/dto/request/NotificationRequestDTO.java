package org.deliveryapp.delivery_service.dto.request;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * delivery-service's own copy of the request body expected by
 * notification-service's POST /api/v1/notifications endpoint.
 * Same independent-contracts rule as MenuItemResponse in order-service:
 * no shared JAR between microservices.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequestDTO {
    private Long userId;
    private String type;     // e.g. "DRIVER_ASSIGNED", "ORDER_DELIVERED"
    private String message;
}
