package org.deliveryapp.payment_service.service;

import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.deliveryapp.payment_service.dto.request.PaymentRequestDTO;
import org.deliveryapp.payment_service.dto.response.PaymentResponseDTO;
import org.deliveryapp.payment_service.event.PaymentApprovedEvent;
import org.deliveryapp.payment_service.event.PaymentEventProducer;
import org.deliveryapp.payment_service.event.PaymentRejectedEvent;
import org.deliveryapp.payment_service.model.enums.PaymentStatus;
import org.deliveryapp.payment_service.repository.IPaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {

    private final IPaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;
    private final MercadoPagoGateway mercadoPagoGateway;   // ← nueva dependencia, otro bean

    @Override
    @Transactional
    public PaymentResponseDTO createPayment(PaymentRequestDTO request) {
        // Llamada a OTRO bean → sí pasa por el proxy de Resilience4j
        Preference preference = mercadoPagoGateway.createPreference(request);

        org.deliveryapp.payment_service.model.Payment payment =
                org.deliveryapp.payment_service.model.Payment.builder()
                        .orderId(request.getOrderId())
                        .userId(request.getUserId())
                        .amount(request.getAmount())
                        .preferenceId(preference.getId())
                        .initPoint(preference.getInitPoint())
                        .status(PaymentStatus.PENDING)
                        .build();

        org.deliveryapp.payment_service.model.Payment saved = paymentRepository.save(payment);

        log.info("Payment created: orderId={}, preferenceId={}",
                request.getOrderId(), preference.getId());

        return toResponse(saved);
    }

    @Override
    @Transactional
    public void processWebhook(String paymentId, String topic) {
        if (!"payment".equals(topic)) {
            log.info("Ignoring webhook topic: {}", topic);
            return;
        }

        Payment mpPayment = mercadoPagoGateway.fetchPayment(paymentId);

        String status = mpPayment.getStatus();
        String externalReference = mpPayment.getExternalReference();
        Long orderId = Long.parseLong(externalReference);

        log.info("Webhook received: paymentId={}, status={}, orderId={}",
                paymentId, status, orderId);

        org.deliveryapp.payment_service.model.Payment payment =
                paymentRepository.findByOrderId(orderId)
                        .orElseThrow(() -> new RuntimeException(
                                "Payment not found for orderId: " + orderId));

        payment.setMpPaymentId(paymentId);

        switch (status) {
            case "approved" -> {
                payment.setStatus(PaymentStatus.APPROVED);
                paymentRepository.save(payment);

                paymentEventProducer.publishPaymentApproved(
                        PaymentApprovedEvent.builder()
                                .orderId(orderId)
                                .userId(payment.getUserId())
                                .mpPaymentId(paymentId)
                                .amount(payment.getAmount())
                                .build()
                );
            }
            case "rejected", "cancelled" -> {
                payment.setStatus(PaymentStatus.REJECTED);
                paymentRepository.save(payment);

                paymentEventProducer.publishPaymentRejected(
                        PaymentRejectedEvent.builder()
                                .orderId(orderId)
                                .userId(payment.getUserId())
                                .reason(status)
                                .build()
                );
            }
            default -> {
                payment.setStatus(PaymentStatus.PENDING);
                paymentRepository.save(payment);
                log.info("Payment still pending: orderId={}, mpStatus={}", orderId, status);
            }
        }
    }

    @Override
    public PaymentResponseDTO getByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException(
                        "Payment not found for orderId: " + orderId));
    }

    private PaymentResponseDTO toResponse(
            org.deliveryapp.payment_service.model.Payment payment) {
        return PaymentResponseDTO.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .mpPaymentId(payment.getMpPaymentId())
                .initPoint(payment.getInitPoint())
                .preferenceId(payment.getPreferenceId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}