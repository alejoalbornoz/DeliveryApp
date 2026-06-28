package service;

import dto.request.OrderRequestDTO;
import dto.response.OrderResponseDTO;
import model.enums.OrderStatus;

import java.util.List;

public interface IOrderService {


    OrderResponseDTO createOrder(OrderRequestDTO request);

    OrderResponseDTO getOrderById(Long id);

    List<OrderResponseDTO> getOrdersByUserId(Long userId);

    OrderResponseDTO updateStatus(Long id, OrderStatus newStatus);

    void cancelOrder(Long id);
}

