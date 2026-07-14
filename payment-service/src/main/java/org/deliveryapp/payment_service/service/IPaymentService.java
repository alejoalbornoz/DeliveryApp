package org.deliveryapp.payment_service.service;

import org.deliveryapp.payment_service.dto.request.PaymentRequestDTO;
import org.deliveryapp.payment_service.dto.response.PaymentResponseDTO;

public interface IPaymentService {

    PaymentResponseDTO createPayment(PaymentRequestDTO request);

    void processWebhook(String paymentId, String topic);

    PaymentResponseDTO getByOrderId(Long orderId);

}
