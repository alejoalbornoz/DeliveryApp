package org.deliveryapp.delivery_service.repository;

import org.deliveryapp.delivery_service.model.Driver;
import org.deliveryapp.delivery_service.model.enums.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IDriverRepository extends JpaRepository <Driver, Long> {

    Optional<Driver> findByUserId(Long userId);

    List<Driver> findByStatus(DriverStatus status);

    // Used to pick the first free driver when assigning a delivery.
    // For a real production system this would consider proximity too,
    // but that's out of scope for this learning project.
    Optional<Driver> findFirstByStatus(DriverStatus status);

}
