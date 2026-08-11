package org.deliveryapp.payment_service.service;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.deliveryapp.payment_service.dto.request.PaymentRequestDTO;
import org.deliveryapp.payment_service.exception.PaymentGatewayUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
public class MercadoPagoGateway {

    @Value("${URL_NGROK}")
    private String baseUrl;

    @CircuitBreaker(name = "mercadoPago", fallbackMethod = "createPreferenceFallback")
    @Retry(name = "mercadoPago")
    public Preference createPreference(PaymentRequestDTO request) {
        try {
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title(request.getDescription())
                    .quantity(1)
                    .unitPrice(request.getAmount())
                    .build();

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
            return client.create(preferenceRequest);

        } catch (MPApiException e) {
            log.error("MercadoPago API error: status={}, content={}",
                    e.getStatusCode(), e.getApiResponse().getContent());
            throw new RuntimeException("MercadoPago API error: " + e.getMessage());
        } catch (MPException e) {
            log.error("MercadoPago error: {}", e.getMessage());
            throw new RuntimeException("MercadoPago error: " + e.getMessage());
        }
    }

    public Preference createPreferenceFallback(PaymentRequestDTO request, Throwable ex) {
        log.error("MercadoPago unavailable, circuit breaker fallback triggered for orderId={}: {}",
                request.getOrderId(), ex.getMessage());
        throw new PaymentGatewayUnavailableException();
    }

    @CircuitBreaker(name = "mercadoPago", fallbackMethod = "fetchPaymentFallback")
    @Retry(name = "mercadoPago")
    public Payment fetchPayment(String paymentId) {
        try {
            PaymentClient client = new PaymentClient();
            return client.get(Long.parseLong(paymentId));
        } catch (MPApiException e) {
            log.error("MercadoPago API error fetching payment: status={}, content={}",
                    e.getStatusCode(), e.getApiResponse().getContent());
            throw new RuntimeException("MercadoPago API error: " + e.getMessage());
        } catch (MPException e) {
            log.error("MercadoPago error fetching payment: {}", e.getMessage());
            throw new RuntimeException("MercadoPago error: " + e.getMessage());
        }
    }

    public Payment fetchPaymentFallback(String paymentId, Throwable ex) {
        log.error("MercadoPago unavailable, cannot fetch paymentId={}: {}",
                paymentId, ex.getMessage());
        throw new PaymentGatewayUnavailableException();
    }
}
