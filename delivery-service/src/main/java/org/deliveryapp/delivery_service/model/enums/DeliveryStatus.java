package org.deliveryapp.delivery_service.model.enums;



public enum DeliveryStatus {
    PENDING_ASSIGNMENT,   // order is ready, waiting for a driver to be assigned
    ASSIGNED,             // a driver has been matched to this delivery
    PICKED_UP,            // driver collected the order from the restaurant
    IN_TRANSIT,           // driver is en route to the customer
    DELIVERED,
    FAILED                // could not be delivered (customer unreachable, etc.)
}
