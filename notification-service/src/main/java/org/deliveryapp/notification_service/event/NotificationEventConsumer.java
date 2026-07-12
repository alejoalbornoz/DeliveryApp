package org.deliveryapp.notification_service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.deliveryapp.notification_service.dto.request.NotificationRequestDTO;
import org.deliveryapp.notification_service.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes delivery status events and triggers notifications.
 * This replaces the Feign call from delivery-service to notification-service.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "delivery-status",
            groupId = "notification-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onDeliveryStatus(DeliveryStatusEvent event) {
        log.info("Received DeliveryStatusEvent: orderId={}, status={}",
                event.getOrderId(), event.getStatus());

        try {
            NotificationRequestDTO request = new NotificationRequestDTO(
                    event.getUserId(),
                    event.getStatus(),
                    event.getMessage()
            );
            notificationService.send(request);
        } catch (Exception e) {
            log.error("Failed to process notification for orderId={}", event.getOrderId(), e);
        }
    }
}