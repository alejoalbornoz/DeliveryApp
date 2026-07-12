package org.deliveryapp.delivery_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Event published by delivery-service when a delivery status changes.
 * Consumed by notification-service to send notifications to the user.
 *
 * delivery-service PRODUCES this event.
 * notification-service has its own copy to CONSUME it.
 * Same independent-contracts rule as the Feign DTOs.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryStatusEvent {

    private Long orderId;
    private Long userId;
    private String status;
    private String message;
}