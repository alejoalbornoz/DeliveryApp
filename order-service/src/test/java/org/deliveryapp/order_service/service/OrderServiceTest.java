package org.deliveryapp.order_service.service;

import org.deliveryapp.order_service.client.RestaurantClient;
import org.deliveryapp.order_service.dto.request.OrderItemRequestDTO;
import org.deliveryapp.order_service.dto.request.OrderRequestDTO;
import org.deliveryapp.order_service.dto.response.MenuItemResponseDTO;
import org.deliveryapp.order_service.dto.response.OrderResponseDTO;
import org.deliveryapp.order_service.event.OrderEventProducer;
import org.deliveryapp.order_service.exception.MenuItemUnavailableException;
import org.deliveryapp.order_service.exception.OrderNotFoundException;
import org.deliveryapp.order_service.model.Order;
import org.deliveryapp.order_service.model.enums.OrderStatus;
import org.deliveryapp.order_service.repository.IOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock
    private IOrderRepository orderRepository;

    @Mock
    private RestaurantClient restaurantClient;

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderEventProducer orderEventProducer;

    private Order sampleOrder;
    private MenuItemResponseDTO sampleMenuItem;
    private OrderRequestDTO sampleRequest;

    @BeforeEach
    void setUp() {
        sampleOrder = Order.builder()
                .id(1L)
                .userId(42L)
                .restaurantId(10L)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(1500.00))
                .deliveryAddress("Av. Corrientes 1234")
                .items(new java.util.ArrayList<>())
                .build();

        sampleMenuItem = MenuItemResponseDTO.builder()
                .id(5L)
                .name("Margherita Pizza")
                .price(BigDecimal.valueOf(1500.00))
                .available(true)
                .restaurantId(10L)
                .build();

        sampleRequest = new OrderRequestDTO(
                42L,
                10L,
                "Av. Corrientes 1234",
                List.of(new OrderItemRequestDTO(5L, 1))
        );
    }

    // ═══════════════════════════════════════════
    //  createOrder()
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("createOrder: should create order when menu item is available")
    void createOrder_shouldCreateOrder_whenMenuItemIsAvailable() {
        // Given
        when(restaurantClient.getMenuItemById(5L)).thenReturn(sampleMenuItem);
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

        // When
        OrderResponseDTO response = orderService.createOrder(sampleRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(42L);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);

        verify(restaurantClient, times(1)).getMenuItemById(5L);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("createOrder: should throw MenuItemUnavailableException when Feign returns null (fallback)")
    void createOrder_shouldThrow_whenFeignFallbackReturnsNull() {
        // Given - null simulates the circuit breaker fallback
        when(restaurantClient.getMenuItemById(5L)).thenReturn(null);

        // When / Then
        assertThatThrownBy(() -> orderService.createOrder(sampleRequest))
                .isInstanceOf(MenuItemUnavailableException.class)
                .hasMessageContaining("5");

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("createOrder: should throw MenuItemUnavailableException when item is not available")
    void createOrder_shouldThrow_whenMenuItemIsUnavailable() {
        // Given - item exists but is marked unavailable
        MenuItemResponseDTO unavailableItem = MenuItemResponseDTO.builder()
                .id(5L)
                .name("Margherita Pizza")
                .price(BigDecimal.valueOf(1500.00))
                .available(false)
                .restaurantId(10L)
                .build();

        when(restaurantClient.getMenuItemById(5L)).thenReturn(unavailableItem);

        // When / Then
        assertThatThrownBy(() -> orderService.createOrder(sampleRequest))
                .isInstanceOf(MenuItemUnavailableException.class);

        verify(orderRepository, never()).save(any());
    }

    // ═══════════════════════════════════════════
    //  getOrderById()
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("getOrderById: should return order when found")
    void getOrderById_shouldReturnOrder_whenFound() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));

        // When
        OrderResponseDTO response = orderService.getOrderById(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("getOrderById: should throw OrderNotFoundException when not found")
    void getOrderById_shouldThrow_whenNotFound() {
        // Given
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> orderService.getOrderById(99L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ═══════════════════════════════════════════
    //  updateStatus()
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("updateStatus: should update order status")
    void updateStatus_shouldUpdateStatus() {
        // Given
        Order confirmed = Order.builder()
                .id(1L)
                .userId(42L)
                .restaurantId(10L)
                .status(OrderStatus.CONFIRMED)
                .totalAmount(BigDecimal.valueOf(1500.00))
                .deliveryAddress("Av. Corrientes 1234")
                .items(new java.util.ArrayList<>())
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(confirmed);


        doNothing().when(orderEventProducer).publishOrderConfirmed(any());

        // When
        OrderResponseDTO response = orderService.updateStatus(1L, OrderStatus.CONFIRMED);

        // Then
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    // ═══════════════════════════════════════════
    //  cancelOrder()
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("cancelOrder: should set status to CANCELLED")
    void cancelOrder_shouldSetStatusToCancelled() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

        // When
        orderService.cancelOrder(1L);

        // Then
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("cancelOrder: should throw when order not found")
    void cancelOrder_shouldThrow_whenNotFound() {
        // Given
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> orderService.cancelOrder(99L))
                .isInstanceOf(OrderNotFoundException.class);

        verify(orderRepository, never()).save(any());
    }
}