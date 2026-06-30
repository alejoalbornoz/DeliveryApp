package org.deliveryapp.order_service.model.enums;

public enum OrderStatus {
    PENDING,        // just created, waiting for restaurant confirmation
    CONFIRMED,      // restaurant accepted the order
    PREPARING,      // restaurant is cooking
    READY_FOR_PICKUP,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}