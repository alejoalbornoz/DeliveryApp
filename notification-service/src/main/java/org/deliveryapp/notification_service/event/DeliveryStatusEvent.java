package org.deliveryapp.notification_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Event published by delivery-service when a delivery status changes.
 * notification-service consumes this to send notifications to the user.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryStatusEvent {

    private Long orderId;
    private Long userId;
    private String status;   // DRIVER_ASSIGNED, ORDER_DELIVERED, etc.
    private String message;
}