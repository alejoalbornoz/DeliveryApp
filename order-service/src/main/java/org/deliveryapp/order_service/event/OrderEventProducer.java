package org.deliveryapp.order_service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private static final String ORDER_CONFIRMED_TOPIC = "order-confirmed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderConfirmed(OrderConfirmedEvent event) {
        kafkaTemplate.send(ORDER_CONFIRMED_TOPIC, String.valueOf(event.getOrderId()), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("OrderConfirmedEvent published: orderId={}, offset={}",
                                event.getOrderId(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to publish OrderConfirmedEvent: orderId={}",
                                event.getOrderId(), ex);
                    }
                });
    }
}