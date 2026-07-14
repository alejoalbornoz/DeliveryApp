package org.deliveryapp.payment_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Published to Kafka when MercadoPago rejects or cancels a payment.
 * order-service consumes this to move the order to CANCELLED status.
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