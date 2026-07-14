package org.deliveryapp.payment_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.deliveryapp.payment_service.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a payment attempt for an order.
 * orderId references order-service by ID only — no cross-service FK.
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // ID returned by MercadoPago after payment is processed
    @Column(name = "mp_payment_id")
    private String mpPaymentId;

    // URL returned by MercadoPago where the customer completes payment
    @Column(name = "init_point", length = 500)
    private String initPoint;

    // ID of the MercadoPago preference created
    @Column(name = "preference_id")
    private String preferenceId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = PaymentStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}