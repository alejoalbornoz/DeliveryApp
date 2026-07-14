package org.deliveryapp.payment_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.deliveryapp.payment_service.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDTO {

    private Long id;
    private Long orderId;
    private Long userId;
    private String mpPaymentId;
    private String initPoint;      // URL where the customer completes payment
    private String preferenceId;
    private BigDecimal amount;
    private PaymentStatus status;
    private LocalDateTime createdAt;
}