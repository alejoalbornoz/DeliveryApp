package service;

import client.RestaurantClient;
import dto.request.OrderItemRequestDTO;
import dto.request.OrderRequestDTO;
import dto.response.MenuItemResponseDTO;
import dto.response.OrderItemResponseDTO;
import dto.response.OrderResponseDTO;
import exception.MenuItemUnavailableException;
import exception.OrderNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Order;
import model.OrderItem;
import model.enums.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.IOrderRepository;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService{


    private final IOrderRepository orderRepository;
    private final RestaurantClient restaurantClient;

    @Override
    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO request) {
        Order order = Order.builder()
                .userId(request.getUserId())
                .restaurantId(request.getRestaurantId())
                .deliveryAddress(request.getDeliveryAddress())
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequestDTO itemRequest : request.getItems()) {
            // Feign call to restaurant-service, guarded by circuit breaker + retry.
            // Returns null if the fallback was triggered (service down or item missing).
            MenuItemResponseDTO menuItem = restaurantClient.getMenuItemById(itemRequest.getMenuItemId());

            if (menuItem == null || !menuItem.isAvailable()) {
                throw new MenuItemUnavailableException(itemRequest.getMenuItemId());
            }

            OrderItem orderItem = OrderItem.builder()
                    .menuItemId(menuItem.getId())
                    .menuItemName(menuItem.getName())       // snapshot at order time
                    .unitPrice(menuItem.getPrice())          // snapshot at order time
                    .quantity(itemRequest.getQuantity())
                    .build();

            order.addItem(orderItem);
            total = total.add(orderItem.getSubtotal());
        }

        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);
        log.info("Order created: id={}, userId={}, total={}",
                saved.getId(), saved.getUserId(), saved.getTotalAmount());

        return toResponse(saved);
    }

    @Override
    public OrderResponseDTO getOrderById(Long id) {
        return toResponse(findOrderOrThrow(id));
    }

    @Override
    public List<OrderResponseDTO> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponseDTO updateStatus(Long id, OrderStatus newStatus) {
        Order order = findOrderOrThrow(id);
        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);
        log.info("Order status updated: id={}, status={}", updated.getId(), newStatus);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void cancelOrder(Long id) {
        Order order = findOrderOrThrow(id);
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Order cancelled: id={}", id);
    }

    private Order findOrderOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    private OrderResponseDTO toResponse(Order order) {
        List<OrderItemResponseDTO> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponseDTO.builder()
                        .id(item.getId())
                        .menuItemId(item.getMenuItemId())
                        .menuItemName(item.getMenuItemName())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getSubtotal())
                        .build())
                .toList();

        return OrderResponseDTO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .restaurantId(order.getRestaurantId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .deliveryAddress(order.getDeliveryAddress())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .build();
    }

}
