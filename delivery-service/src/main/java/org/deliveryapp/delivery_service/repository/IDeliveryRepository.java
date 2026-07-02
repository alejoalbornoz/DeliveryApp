package org.deliveryapp.delivery_service.repository;

import org.deliveryapp.delivery_service.model.Delivery;
import org.deliveryapp.delivery_service.model.enums.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IDeliveryRepository extends JpaRepository <Delivery, Long> {


    Optional<Delivery> findByOrderId(Long orderId);

    List<Delivery> findByStatus(DeliveryStatus status);

    List<Delivery> findByDriverId(Long driverId);

}
