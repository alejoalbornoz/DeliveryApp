package org.deliveryapp.restaurant_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.deliveryapp.restaurant_service.dto.request.RestaurantRequestDTO;
import org.deliveryapp.restaurant_service.dto.response.RestaurantResponseDTO;
import org.deliveryapp.restaurant_service.exception.RestaurantNotFoundException;
import org.deliveryapp.restaurant_service.model.enums.RestaurantStatus;
import org.deliveryapp.restaurant_service.service.RestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(RestaurantController.class)
@Import(RestaurantController.class)
@DisplayName("RestaurantController Unit Tests")
class RestaurantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RestaurantService restaurantService;

    private RestaurantRequestDTO sampleRequest;
    private RestaurantResponseDTO sampleResponse;

    @BeforeEach
    void setUp() {
        sampleRequest = new RestaurantRequestDTO(
                "La Pizzeria",
                "Best pizza in town",
                "Av. Corrientes 1234",
                "011-4444-5555",
                10L
        );

        sampleResponse = RestaurantResponseDTO.builder()
                .id(1L)
                .name("La Pizzeria")
                .description("Best pizza in town")
                .address("Av. Corrientes 1234")
                .phone("011-4444-5555")
                .ownerId(10L)
                .status(RestaurantStatus.OPEN)
                .rating(0.0)
                .build();
    }

    // ═══════════════════════════════════════════
    //  POST /api/v1/restaurants
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("create: should return 201 with restaurant")
    void create_shouldReturn201() throws Exception {
        when(restaurantService.create(any(RestaurantRequestDTO.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("La Pizzeria"))
                .andExpect(jsonPath("$.status").value("OPEN"));

        verify(restaurantService, times(1)).create(any(RestaurantRequestDTO.class));
    }

    @Test
    @DisplayName("create: should return 400 when name is missing")
    void create_shouldReturn400_whenNameIsMissing() throws Exception {
        RestaurantRequestDTO invalid = new RestaurantRequestDTO(
                "", "desc", "address", "011-1111-2222", 10L);

        mockMvc.perform(post("/api/v1/restaurants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verify(restaurantService, never()).create(any());
    }

    // ═══════════════════════════════════════════
    //  GET /api/v1/restaurants/{id}
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("getById: should return 200 with restaurant")
    void getById_shouldReturn200() throws Exception {
        when(restaurantService.getById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/restaurants/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("La Pizzeria"));
    }

    @Test
    @DisplayName("getById: should return 404 when not found")
    void getById_shouldReturn404_whenNotFound() throws Exception {
        when(restaurantService.getById(99L))
                .thenThrow(new RestaurantNotFoundException(99L));

        mockMvc.perform(get("/api/v1/restaurants/99"))
                .andExpect(status().isNotFound());
    }

    // ═══════════════════════════════════════════
    //  GET /api/v1/restaurants
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("getAll: should return 200 with list")
    void getAll_shouldReturn200WithList() throws Exception {
        when(restaurantService.getAll()).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/api/v1/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("La Pizzeria"));
    }

    // ═══════════════════════════════════════════
    //  PUT /api/v1/restaurants/{id}
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("update: should return 200 with updated restaurant")
    void update_shouldReturn200() throws Exception {
        when(restaurantService.update(eq(1L), any(RestaurantRequestDTO.class)))
                .thenReturn(sampleResponse);

        mockMvc.perform(put("/api/v1/restaurants/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("update: should return 404 when restaurant not found")
    void update_shouldReturn404_whenNotFound() throws Exception {
        when(restaurantService.update(eq(99L), any(RestaurantRequestDTO.class)))
                .thenThrow(new RestaurantNotFoundException(99L));

        mockMvc.perform(put("/api/v1/restaurants/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().isNotFound());
    }

    // ═══════════════════════════════════════════
    //  DELETE /api/v1/restaurants/{id}
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("delete: should return 204")
    void delete_shouldReturn204() throws Exception {
        doNothing().when(restaurantService).delete(1L);

        mockMvc.perform(delete("/api/v1/restaurants/1"))
                .andExpect(status().isNoContent());

        verify(restaurantService, times(1)).delete(1L);
    }

    @Test
    @DisplayName("delete: should return 404 when not found")
    void delete_shouldReturn404_whenNotFound() throws Exception {
        doThrow(new RestaurantNotFoundException(99L))
                .when(restaurantService).delete(99L);

        mockMvc.perform(delete("/api/v1/restaurants/99"))
                .andExpect(status().isNotFound());
    }
}