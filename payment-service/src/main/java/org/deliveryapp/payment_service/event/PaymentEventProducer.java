package org.deliveryapp.payment_service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private static final String PAYMENT_APPROVED_TOPIC = "payment-approved";
    private static final String PAYMENT_REJECTED_TOPIC = "payment-rejected";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaymentApproved(PaymentApprovedEvent event) {
        kafkaTemplate.send(PAYMENT_APPROVED_TOPIC,
                        String.valueOf(event.getOrderId()), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("PaymentApprovedEvent published: orderId={}", event.getOrderId());
                    } else {
                        log.error("Failed to publish PaymentApprovedEvent: orderId={}",
                                event.getOrderId(), ex);
                    }
                });
    }

    public void publishPaymentRejected(PaymentRejectedEvent event) {
        kafkaTemplate.send(PAYMENT_REJECTED_TOPIC,
                        String.valueOf(event.getOrderId()), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("PaymentRejectedEvent published: orderId={}", event.getOrderId());
                    } else {
                        log.error("Failed to publish PaymentRejectedEvent: orderId={}",
                                event.getOrderId(), ex);
                    }
                });
    }
}