package org.deliveryapp.delivery_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class NoAvailableDriverException extends RuntimeException {

    public NoAvailableDriverException() {
        super("No available drivers at the moment. Please try again shortly.");
    }
}
