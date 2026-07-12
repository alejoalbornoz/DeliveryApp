package org.deliveryapp.order_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.deliveryapp.order_service.dto.request.OrderItemRequestDTO;
import org.deliveryapp.order_service.dto.request.OrderRequestDTO;
import org.deliveryapp.order_service.dto.response.OrderResponseDTO;
import org.deliveryapp.order_service.exception.OrderNotFoundException;
import org.deliveryapp.order_service.model.enums.OrderStatus;
import org.deliveryapp.order_service.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(OrderController.class)
@DisplayName("OrderController Unit Tests")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    private OrderRequestDTO sampleRequest;
    private OrderResponseDTO sampleResponse;

    @BeforeEach
    void setUp() {
        sampleRequest = new OrderRequestDTO(
                42L,
                10L,
                "Av. Corrientes 1234",
                List.of(new OrderItemRequestDTO(5L, 2))
        );

        sampleResponse = OrderResponseDTO.builder()
                .id(1L)
                .userId(42L)
                .restaurantId(10L)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(3000.00))
                .deliveryAddress("Av. Corrientes 1234")
                .items(List.of())
                .build();
    }

    // ═══════════════════════════════════════════
    //  POST /api/v1/orders
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("create: should return 201 with order")
    void create_shouldReturn201() throws Exception {
        when(orderService.createOrder(any(OrderRequestDTO.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.userId").value(42));

        verify(orderService, times(1)).createOrder(any(OrderRequestDTO.class));
    }

    @Test
    @DisplayName("create: should return 400 when items list is empty")
    void create_shouldReturn400_whenItemsIsEmpty() throws Exception {
        OrderRequestDTO invalid = new OrderRequestDTO(42L, 10L, "address", List.of());

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(any());
    }

    // ═══════════════════════════════════════════
    //  GET /api/v1/orders/{id}
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("getById: should return 200 with order")
    void getById_shouldReturn200() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("getById: should return 404 when not found")
    void getById_shouldReturn404_whenNotFound() throws Exception {
        when(orderService.getOrderById(99L))
                .thenThrow(new OrderNotFoundException(99L));

        mockMvc.perform(get("/api/v1/orders/99"))
                .andExpect(status().isNotFound());
    }

    // ═══════════════════════════════════════════
    //  GET /api/v1/orders/user/{userId}
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("getByUserId: should return 200 with list")
    void getByUserId_shouldReturn200() throws Exception {
        when(orderService.getOrdersByUserId(42L)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/orders/user/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(42));
    }

    // ═══════════════════════════════════════════
    //  PATCH /api/v1/orders/{id}/status
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("updateStatus: should return 200 with updated order")
    void updateStatus_shouldReturn200() throws Exception {
        OrderResponseDTO confirmed = OrderResponseDTO.builder()
                .id(1L)
                .userId(42L)
                .restaurantId(10L)
                .status(OrderStatus.CONFIRMED)
                .totalAmount(BigDecimal.valueOf(3000.00))
                .deliveryAddress("Av. Corrientes 1234")
                .items(List.of())
                .build();

        when(orderService.updateStatus(eq(1L), eq(OrderStatus.CONFIRMED)))
                .thenReturn(confirmed);

        mockMvc.perform(patch("/api/v1/orders/1/status")
                        .param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    // ═══════════════════════════════════════════
    //  PATCH /api/v1/orders/{id}/cancel
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("cancel: should return 204")
    void cancel_shouldReturn204() throws Exception {
        doNothing().when(orderService).cancelOrder(1L);

        mockMvc.perform(patch("/api/v1/orders/1/cancel"))
                .andExpect(status().isNoContent());

        verify(orderService, times(1)).cancelOrder(1L);
    }

    @Test
    @DisplayName("cancel: should return 404 when order not found")
    void cancel_shouldReturn404_whenNotFound() throws Exception {
        doThrow(new OrderNotFoundException(99L))
                .when(orderService).cancelOrder(99L);

        mockMvc.perform(patch("/api/v1/orders/99/cancel"))
                .andExpect(status().isNotFound());
    }
}