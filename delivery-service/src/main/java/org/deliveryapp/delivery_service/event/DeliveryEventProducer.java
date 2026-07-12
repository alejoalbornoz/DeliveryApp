package org.deliveryapp.delivery_service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryEventProducer {

    private static final String DELIVERY_STATUS_TOPIC = "delivery-status";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishDeliveryStatus(DeliveryStatusEvent event) {
        kafkaTemplate.send(DELIVERY_STATUS_TOPIC,
                        String.valueOf(event.getOrderId()), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("DeliveryStatusEvent published: orderId={}, status={}",
                                event.getOrderId(), event.getStatus());
                    } else {
                        log.error("Failed to publish DeliveryStatusEvent: orderId={}",
                                event.getOrderId(), ex);
                    }
                });
    }
}