package org.deliveryapp.delivery_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * delivery-service's own copy of the event published by order-service.
 * Jackson matches fields by name during deserialization.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderConfirmedEvent {

    private Long orderId;
    private Long userId;
    private Long restaurantId;
    private String deliveryAddress;
    private BigDecimal totalAmount;
    private LocalDateTime confirmedAt;
}