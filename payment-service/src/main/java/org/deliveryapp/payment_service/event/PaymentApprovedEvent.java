package org.deliveryapp.payment_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Published to Kafka when MercadoPago confirms a payment.
 * order-service consumes this to move the order to CONFIRMED status.
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