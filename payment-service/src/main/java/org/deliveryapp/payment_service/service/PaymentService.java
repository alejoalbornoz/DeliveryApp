package org.deliveryapp.payment_service.service;

import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import com.mercadopago.resources.payment.Payment;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.deliveryapp.payment_service.dto.request.PaymentRequestDTO;
import org.deliveryapp.payment_service.dto.response.PaymentResponseDTO;
import org.deliveryapp.payment_service.event.PaymentApprovedEvent;
import org.deliveryapp.payment_service.event.PaymentEventProducer;
import org.deliveryapp.payment_service.event.PaymentRejectedEvent;
import org.deliveryapp.payment_service.model.enums.PaymentStatus;
import org.deliveryapp.payment_service.repository.IPaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {

    private final IPaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;

    @Value("URL_DE_NGROK")
    private String baseUrl;

    @Override
    @Transactional
    public PaymentResponseDTO createPayment(PaymentRequestDTO request) {
        try {
            // Build the item for the MercadoPago preference
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title(request.getDescription())
                    .quantity(1)
                    .unitPrice(request.getAmount())
                    .build();

            // Back URLs — where MP redirects the customer after payment


// Y en el método:
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(baseUrl + "/api/v1/payments/success")
                    .failure(baseUrl + "/api/v1/payments/failure")
                    .pending(baseUrl + "/api/v1/payments/pending")
                    .build();

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(List.of(item))
                    .backUrls(backUrls)
                    .autoReturn("approved")
                    .externalReference(String.valueOf(request.getOrderId()))
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            // Persist the payment record with PENDING status
            org.deliveryapp.payment_service.model.Payment payment =
                    org.deliveryapp.payment_service.model.Payment.builder()
                            .orderId(request.getOrderId())
                            .userId(request.getUserId())
                            .amount(request.getAmount())
                            .preferenceId(preference.getId())
                            .initPoint(preference.getInitPoint())
                            .status(PaymentStatus.PENDING)
                            .build();

            org.deliveryapp.payment_service.model.Payment saved =
                    paymentRepository.save(payment);

            log.info("Payment created: orderId={}, preferenceId={}",
                    request.getOrderId(), preference.getId());

            return toResponse(saved);

        } catch (MPApiException e) {
            log.error("MercadoPago API error: status={}, content={}",
                    e.getStatusCode(),
                    e.getApiResponse().getContent());
            throw new RuntimeException("Failed to create MercadoPago preference: " + e.getMessage());
        } catch (MPException e) {
            log.error("MercadoPago error: {}", e.getMessage());
            throw new RuntimeException("Failed to create MercadoPago preference: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void processWebhook(String paymentId, String topic) {
        // MercadoPago sends topic="payment" for payment notifications
        if (!"payment".equals(topic)) {
            log.info("Ignoring webhook topic: {}", topic);
            return;
        }

        try {
            PaymentClient client = new PaymentClient();
            Payment mpPayment = client.get(Long.parseLong(paymentId));

            String status = mpPayment.getStatus();
            String externalReference = mpPayment.getExternalReference();
            Long orderId = Long.parseLong(externalReference);

            log.info("Webhook received: paymentId={}, status={}, orderId={}",
                    paymentId, status, orderId);

            // Find the local payment record and update it
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

        } catch (MPException | MPApiException e) {
            log.error("Error processing webhook for paymentId={}", paymentId, e);
            throw new RuntimeException("Failed to process webhook: " + e.getMessage());
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