package org.deliveryapp.order_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MenuItemUnavailableException extends RuntimeException {

    public MenuItemUnavailableException(Long menuItemId) {
        super("Menu item " + menuItemId + " is unavailable or does not exist");
    }
}
