package org.deliveryapp.order_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * order-service's own copy of the event published by payment-service.
 * Consumed to move the order status to CONFIRMED.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentApprovedEvent {

    private Long orderId;
    private Long userId;
    private String mpPaymentId;
    private BigDecimal amount;
}