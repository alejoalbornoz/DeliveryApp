package org.deliveryapp.delivery_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.deliveryapp.delivery_service.dto.request.DeliveryRequestDTO;
import org.deliveryapp.delivery_service.dto.response.DeliveryResponseDTO;
import org.deliveryapp.delivery_service.exception.DeliveryNotFoundException;
import org.deliveryapp.delivery_service.exception.NoAvailableDriverException;
import org.deliveryapp.delivery_service.model.enums.DeliveryStatus;
import org.deliveryapp.delivery_service.service.DeliveryService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(DeliveryController.class)
@Import(DeliveryController.class)
@DisplayName("DeliveryController Unit Tests")
class DeliveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DeliveryService deliveryService;

    private DeliveryRequestDTO sampleRequest;
    private DeliveryResponseDTO sampleResponse;

    @BeforeEach
    void setUp() {
        sampleRequest = new DeliveryRequestDTO(42L, "Av. Corrientes 1234");

        sampleResponse = DeliveryResponseDTO.builder()
                .id(1L)
                .orderId(42L)
                .deliveryAddress("Av. Corrientes 1234")
                .status(DeliveryStatus.PENDING_ASSIGNMENT)
                .build();
    }

    // ═══════════════════════════════════════════
    //  POST /api/v1/delivery
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("create: should return 201 with delivery")
    void create_shouldReturn201() throws Exception {
        when(deliveryService.createDelivery(any(DeliveryRequestDTO.class)))
                .thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/delivery")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderId").value(42))
                .andExpect(jsonPath("$.status").value("PENDING_ASSIGNMENT"));

        verify(deliveryService, times(1)).createDelivery(any(DeliveryRequestDTO.class));
    }

    @Test
    @DisplayName("create: should return 400 when orderId is missing")
    void create_shouldReturn400_whenOrderIdIsMissing() throws Exception {
        DeliveryRequestDTO invalid = new DeliveryRequestDTO(null, "Av. Corrientes 1234");

        mockMvc.perform(post("/api/v1/delivery")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verify(deliveryService, never()).createDelivery(any());
    }

    // ═══════════════════════════════════════════
    //  GET /api/v1/delivery/{id}
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("getById: should return 200 with delivery")
    void getById_shouldReturn200() throws Exception {
        when(deliveryService.getById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/delivery/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderId").value(42));
    }

    @Test
    @DisplayName("getById: should return 404 when not found")
    void getById_shouldReturn404_whenNotFound() throws Exception {
        when(deliveryService.getById(99L))
                .thenThrow(new DeliveryNotFoundException(99L));

        mockMvc.perform(get("/api/v1/delivery/99"))
                .andExpect(status().isNotFound());
    }

    // ═══════════════════════════════════════════
    //  GET /api/v1/delivery/order/{orderId}
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("getByOrderId: should return 200 with delivery")
    void getByOrderId_shouldReturn200() throws Exception {
        when(deliveryService.getByOrderId(42L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/delivery/order/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(42));
    }

    // ═══════════════════════════════════════════
    //  PATCH /api/v1/delivery/{id}/assign
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("assignDriver: should return 200 when driver assigned")
    void assignDriver_shouldReturn200() throws Exception {
        DeliveryResponseDTO assigned = DeliveryResponseDTO.builder()
                .id(1L)
                .orderId(42L)
                .driverId(1L)
                .driverName("Carlos Repartidor")
                .deliveryAddress("Av. Corrientes 1234")
                .status(DeliveryStatus.ASSIGNED)
                .build();

        when(deliveryService.assignDriver(1L)).thenReturn(assigned);

        mockMvc.perform(patch("/api/v1/delivery/1/assign"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.driverId").value(1));
    }

    @Test
    @DisplayName("assignDriver: should return 409 when no drivers available")
    void assignDriver_shouldReturn409_whenNoDrivers() throws Exception {
        when(deliveryService.assignDriver(1L))
                .thenThrow(new NoAvailableDriverException());

        mockMvc.perform(patch("/api/v1/delivery/1/assign"))
                .andExpect(status().isConflict());
    }

    // ═══════════════════════════════════════════
    //  PATCH /api/v1/delivery/{id}/status
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("updateStatus: should return 200 with updated delivery")
    void updateStatus_shouldReturn200() throws Exception {
        DeliveryResponseDTO delivered = DeliveryResponseDTO.builder()
                .id(1L)
                .orderId(42L)
                .deliveryAddress("Av. Corrientes 1234")
                .status(DeliveryStatus.DELIVERED)
                .build();

        when(deliveryService.updateStatus(eq(1L), eq(DeliveryStatus.DELIVERED)))
                .thenReturn(delivered);

        mockMvc.perform(patch("/api/v1/delivery/1/status")
                        .param("status", "DELIVERED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));
    }
}