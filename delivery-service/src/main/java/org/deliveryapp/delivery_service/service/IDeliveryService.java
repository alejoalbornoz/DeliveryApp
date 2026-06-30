package org.deliveryapp.delivery_service.service;

import org.deliveryapp.delivery_service.dto.request.DeliveryRequestDTO;
import org.deliveryapp.delivery_service.dto.response.DeliveryResponseDTO;
import org.deliveryapp.delivery_service.model.enums.DeliveryStatus;

public interface IDeliveryService {

    DeliveryResponseDTO createDelivery(DeliveryRequestDTO request);

    DeliveryResponseDTO getById(Long id);

    DeliveryResponseDTO getByOrderId(Long orderId);

    DeliveryResponseDTO assignDriver(Long deliveryId);

    DeliveryResponseDTO updateStatus(Long deliveryId, DeliveryStatus newStatus);
}
