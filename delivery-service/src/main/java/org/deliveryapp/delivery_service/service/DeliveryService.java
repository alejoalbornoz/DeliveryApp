package org.deliveryapp.delivery_service.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.deliveryapp.delivery_service.client.NotificationClient;
import org.deliveryapp.delivery_service.dto.request.DeliveryRequestDTO;
import org.deliveryapp.delivery_service.dto.request.NotificationRequestDTO;
import org.deliveryapp.delivery_service.dto.response.DeliveryResponseDTO;
import org.deliveryapp.delivery_service.event.DeliveryEventProducer;
import org.deliveryapp.delivery_service.event.DeliveryStatusEvent;
import org.deliveryapp.delivery_service.exception.DeliveryNotFoundException;
import org.deliveryapp.delivery_service.exception.NoAvailableDriverException;
import org.deliveryapp.delivery_service.model.Delivery;
import org.deliveryapp.delivery_service.model.Driver;
import org.deliveryapp.delivery_service.model.enums.DeliveryStatus;
import org.deliveryapp.delivery_service.model.enums.DriverStatus;
import org.deliveryapp.delivery_service.repository.IDeliveryRepository;
import org.deliveryapp.delivery_service.repository.IDriverRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService implements IDeliveryService{

    private final IDeliveryRepository deliveryRepository;
    private final IDriverRepository driverRepository;
    private final NotificationClient notificationClient;
    private final DeliveryEventProducer deliveryEventProducer;


    @Override
    @Transactional
    public DeliveryResponseDTO createDelivery(DeliveryRequestDTO request) {
        Delivery delivery = Delivery.builder()
                .orderId(request.getOrderId())
                .deliveryAddress(request.getDeliveryAddress())
                .build();

        Delivery saved = deliveryRepository.save(delivery);
        log.info("Delivery created: id={}, orderId={}", saved.getId(), saved.getOrderId());
        return toResponse(saved);
    }

    @Override
    public DeliveryResponseDTO getById(Long id) {
        return toResponse(findDeliveryOrThrow(id));
    }

    @Override
    public DeliveryResponseDTO getByOrderId(Long orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new DeliveryNotFoundException(orderId));
        return toResponse(delivery);
    }

    @Override
    @Transactional
    public DeliveryResponseDTO assignDriver(Long deliveryId) {
        Delivery delivery = findDeliveryOrThrow(deliveryId);

        Driver driver = driverRepository.findFirstByStatus(DriverStatus.AVAILABLE)
                .orElseThrow(NoAvailableDriverException::new);

        driver.setStatus(DriverStatus.ON_DELIVERY);
        driverRepository.save(driver);

        delivery.setDriver(driver);
        delivery.setStatus(DeliveryStatus.ASSIGNED);
        Delivery updated = deliveryRepository.save(delivery);

        log.info("Driver assigned: deliveryId={}, driverId={}", updated.getId(), driver.getId());

        // Best-effort notification — failure here must NOT roll back the assignment.
        deliveryEventProducer.publishDeliveryStatus(
                DeliveryStatusEvent.builder()
                        .orderId(delivery.getOrderId())
                        .userId(driver.getUserId())
                        .status("DRIVER_ASSIGNED")
                        .message("You've been assigned delivery #" + delivery.getOrderId())
                        .build()
        );


        return toResponse(updated);
    }

    @Override
    @Transactional
    public DeliveryResponseDTO updateStatus(Long deliveryId, DeliveryStatus newStatus) {
        Delivery delivery = findDeliveryOrThrow(deliveryId);
        delivery.setStatus(newStatus);
        Delivery updated = deliveryRepository.save(delivery);

        // Free up the driver once the delivery reaches a terminal state
        if ((newStatus == DeliveryStatus.DELIVERED || newStatus == DeliveryStatus.FAILED)
                && updated.getDriver() != null) {

            Driver driver = updated.getDriver();
            driver.setStatus(DriverStatus.AVAILABLE);
            driverRepository.save(driver);

            deliveryEventProducer.publishDeliveryStatus(
                    DeliveryStatusEvent.builder()
                            .orderId(delivery.getOrderId())
                            .userId(driver.getUserId())
                            .status("ORDER_" + newStatus.name())
                            .message("Your order #" + delivery.getOrderId() + " status: " + newStatus)
                            .build()
            );
        }

        log.info("Delivery status updated: id={}, status={}", updated.getId(), newStatus);
        return toResponse(updated);
    }

    private Delivery findDeliveryOrThrow(Long id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new DeliveryNotFoundException(id));
    }

    private DeliveryResponseDTO toResponse(Delivery delivery) {
        return DeliveryResponseDTO.builder()
                .id(delivery.getId())
                .orderId(delivery.getOrderId())
                .driverId(delivery.getDriver() != null ? delivery.getDriver().getId() : null)
                .driverName(delivery.getDriver() != null ? delivery.getDriver().getName() : null)
                .deliveryAddress(delivery.getDeliveryAddress())
                .status(delivery.getStatus())
                .createdAt(delivery.getCreatedAt())
                .build();
    }
}
