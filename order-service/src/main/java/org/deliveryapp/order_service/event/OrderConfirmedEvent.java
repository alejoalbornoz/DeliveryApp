package org.deliveryapp.order_service.event;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
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
