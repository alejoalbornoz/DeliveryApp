package org.deliveryapp.notification_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.deliveryapp.notification_service.dto.request.NotificationRequestDTO;
import org.deliveryapp.notification_service.dto.response.NotificationResponseDTO;
import org.deliveryapp.notification_service.model.enums.NotificationType;
import org.deliveryapp.notification_service.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(NotificationController.class)
@DisplayName("NotificationController Unit Tests")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NotificationService notificationService;

    private NotificationRequestDTO sampleRequest;
    private NotificationResponseDTO sampleResponse;

    @BeforeEach
    void setUp() {
        sampleRequest = new NotificationRequestDTO(
                42L, "DRIVER_ASSIGNED", "Your driver is on the way");

        sampleResponse = NotificationResponseDTO.builder()
                .id(1L)
                .userId(42L)
                .type(NotificationType.DRIVER_ASSIGNED)
                .message("Your driver is on the way")
                .sent(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ═══════════════════════════════════════════
    //  POST /api/v1/notifications
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("send: should return 201 with notification")
    void send_shouldReturn201() throws Exception {
        when(notificationService.send(any(NotificationRequestDTO.class)))
                .thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(42))
                .andExpect(jsonPath("$.type").value("DRIVER_ASSIGNED"))
                .andExpect(jsonPath("$.sent").value(true));

        verify(notificationService, times(1)).send(any(NotificationRequestDTO.class));
    }

    @Test
    @DisplayName("send: should return 400 when userId is missing")
    void send_shouldReturn400_whenUserIdIsMissing() throws Exception {
        NotificationRequestDTO invalid = new NotificationRequestDTO(
                null, "DRIVER_ASSIGNED", "message");

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verify(notificationService, never()).send(any());
    }

    @Test
    @DisplayName("send: should return 400 when message is blank")
    void send_shouldReturn400_whenMessageIsBlank() throws Exception {
        NotificationRequestDTO invalid = new NotificationRequestDTO(42L, "DRIVER_ASSIGNED", "");

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verify(notificationService, never()).send(any());
    }

    // ═══════════════════════════════════════════
    //  GET /api/v1/notifications/user/{userId}
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("getByUserId: should return 200 with list of notifications")
    void getByUserId_shouldReturn200() throws Exception {
        NotificationResponseDTO second = NotificationResponseDTO.builder()
                .id(2L)
                .userId(42L)
                .type(NotificationType.ORDER_DELIVERED)
                .message("Your order arrived!")
                .sent(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(notificationService.getByUserId(42L))
                .thenReturn(List.of(sampleResponse, second));

        mockMvc.perform(get("/api/v1/notifications/user/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value("DRIVER_ASSIGNED"))
                .andExpect(jsonPath("$[1].type").value("ORDER_DELIVERED"));
    }

    @Test
    @DisplayName("getByUserId: should return 200 with empty list when no notifications")
    void getByUserId_shouldReturn200_withEmptyList() throws Exception {
        when(notificationService.getByUserId(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/notifications/user/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}