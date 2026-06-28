package org.deliveryapp.delivery_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.deliveryapp.delivery_service.model.enums.DriverStatus;

/**
 * A delivery driver.
 *
 * userId references a User from auth-service (the same person who
 * registered with ROLE_DRIVER) — plain Long, same cross-service
 * reference pattern used everywhere else in this project.
 */
@Entity
@Table(name = "drivers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(name = "vehicle_plate", length = 20)
    private String vehiclePlate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DriverStatus status;

    @PrePersist
    protected void onCreate() {
        if (this.status == null) this.status = DriverStatus.AVAILABLE;
    }
}
