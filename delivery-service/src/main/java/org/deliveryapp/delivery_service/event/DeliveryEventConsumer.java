package org.deliveryapp.delivery_service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.deliveryapp.delivery_service.dto.request.DeliveryRequestDTO;
import org.deliveryapp.delivery_service.service.DeliveryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes events from order-service and triggers delivery creation.
 * This replaces the synchronous Feign call that order-service would
 * have made to delivery-service — the order service no longer needs
 * to know delivery-service exists.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryEventConsumer {

    private final DeliveryService deliveryService;

    @KafkaListener(
            topics = "order-confirmed",
            groupId = "delivery-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        log.info("Received OrderConfirmedEvent: orderId={}", event.getOrderId());

        try {
            DeliveryRequestDTO request = new DeliveryRequestDTO(
                    event.getOrderId(),
                    event.getDeliveryAddress()
            );
            deliveryService.createDelivery(request);
            log.info("Delivery created for orderId={}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to create delivery for orderId={}", event.getOrderId(), e);
            // In production: send to a dead-letter topic for retry
        }
    }
}