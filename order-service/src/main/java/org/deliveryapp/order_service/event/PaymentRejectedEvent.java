package org.deliveryapp.order_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * order-service's own copy of the event published by payment-service.
 * Consumed to move the order status to CANCELLED.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRejectedEvent {

    private Long orderId;
    private Long userId;
    private String reason;
}