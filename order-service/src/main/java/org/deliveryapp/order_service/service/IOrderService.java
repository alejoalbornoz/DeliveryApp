package org.deliveryapp.order_service.service;

import org.deliveryapp.order_service.dto.request.OrderRequestDTO;
import org.deliveryapp.order_service.dto.response.OrderResponseDTO;
import org.deliveryapp.order_service.model.enums.OrderStatus;

import java.util.List;

public interface IOrderService {


    OrderResponseDTO createOrder(OrderRequestDTO request);

    OrderResponseDTO getOrderById(Long id);

    List<OrderResponseDTO> getOrdersByUserId(Long userId);

    OrderResponseDTO updateStatus(Long id, OrderStatus newStatus);

    void cancelOrder(Long id);
}

