package org.deliveryapp.payment_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class PaymentGatewayUnavailableException extends RuntimeException {

    public PaymentGatewayUnavailableException() {
        super("Payment gateway is currently unavailable. Please try again shortly.");
    }
}