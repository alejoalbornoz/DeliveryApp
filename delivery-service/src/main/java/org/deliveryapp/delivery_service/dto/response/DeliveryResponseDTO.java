package org.deliveryapp.delivery_service.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.deliveryapp.delivery_service.model.enums.DeliveryStatus;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryResponseDTO {

    private Long id;
    private Long orderId;
    private Long driverId;
    private String driverName;
    private String deliveryAddress;
    private DeliveryStatus status;
    private LocalDateTime createdAt;
}
