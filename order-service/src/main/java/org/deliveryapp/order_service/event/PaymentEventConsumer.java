package org.deliveryapp.order_service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.deliveryapp.order_service.model.enums.OrderStatus;
import org.deliveryapp.order_service.service.OrderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final OrderService orderService;

    @KafkaListener(
            topics = "payment-approved",
            groupId = "order-service-payment-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onPaymentApproved(PaymentApprovedEvent event) {
        log.info("PaymentApprovedEvent received: orderId={}", event.getOrderId());
        try {
            orderService.updateStatus(event.getOrderId(), OrderStatus.CONFIRMED);
            log.info("Order confirmed after payment: orderId={}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to confirm order after payment: orderId={}",
                    event.getOrderId(), e);
        }
    }

    @KafkaListener(
            topics = "payment-rejected",
            groupId = "order-service-payment-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onPaymentRejected(PaymentRejectedEvent event) {
        log.info("PaymentRejectedEvent received: orderId={}, reason={}",
                event.getOrderId(), event.getReason());
        try {
            orderService.cancelOrder(event.getOrderId());
            log.info("Order cancelled after payment rejection: orderId={}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to cancel order after payment rejection: orderId={}",
                    event.getOrderId(), e);
        }
    }
}