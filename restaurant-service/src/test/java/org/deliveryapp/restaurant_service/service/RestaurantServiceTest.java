package org.deliveryapp.restaurant_service.service;

import org.deliveryapp.restaurant_service.dto.request.RestaurantRequestDTO;
import org.deliveryapp.restaurant_service.dto.response.RestaurantResponseDTO;
import org.deliveryapp.restaurant_service.exception.RestaurantNotFoundException;
import org.deliveryapp.restaurant_service.model.Restaurant;
import org.deliveryapp.restaurant_service.model.enums.RestaurantStatus;
import org.deliveryapp.restaurant_service.repository.IRestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RestaurantService Unit Tests")
class RestaurantServiceTest {

    @Mock
    private IRestaurantRepository restaurantRepository;

    @InjectMocks
    private RestaurantService restaurantService;

    private Restaurant sampleRestaurant;
    private RestaurantRequestDTO sampleRequest;

    @BeforeEach
    void setUp() {
        sampleRestaurant = Restaurant.builder()
                .id(1L)
                .name("La Pizzeria")
                .description("Best pizza in town")
                .address("Av. Corrientes 1234")
                .phone("011-4444-5555")
                .ownerId(10L)
                .status(RestaurantStatus.OPEN)
                .rating(0.0)
                .build();

        sampleRequest = new RestaurantRequestDTO(
                "La Pizzeria",
                "Best pizza in town",
                "Av. Corrientes 1234",
                "011-4444-5555",
                10L
        );
    }

    // ═══════════════════════════════════════════
    //  create()
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("create: should save and return restaurant")
    void create_shouldSaveAndReturnRestaurant() {
        // Given
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(sampleRestaurant);

        // When
        RestaurantResponseDTO response = restaurantService.create(sampleRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("La Pizzeria");
        assertThat(response.getOwnerId()).isEqualTo(10L);
        assertThat(response.getStatus()).isEqualTo(RestaurantStatus.OPEN);

        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
    }

    // ═══════════════════════════════════════════
    //  getById()
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("getById: should return restaurant when found")
    void getById_shouldReturnRestaurant_whenFound() {
        // Given
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(sampleRestaurant));

        // When
        RestaurantResponseDTO response = restaurantService.getById(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("La Pizzeria");
    }

    @Test
    @DisplayName("getById: should throw RestaurantNotFoundException when not found")
    void getById_shouldThrow_whenNotFound() {
        // Given
        when(restaurantRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> restaurantService.getById(99L))
                .isInstanceOf(RestaurantNotFoundException.class)
                .hasMessageContaining("99");

        verify(restaurantRepository, times(1)).findById(99L);
    }

    // ═══════════════════════════════════════════
    //  getAll()
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("getAll: should return list of restaurants")
    void getAll_shouldReturnList() {
        // Given
        Restaurant second = Restaurant.builder()
                .id(2L)
                .name("El Burger")
                .address("Av. Santa Fe 5678")
                .phone("011-5555-6666")
                .ownerId(20L)
                .status(RestaurantStatus.OPEN)
                .rating(0.0)
                .build();

        when(restaurantRepository.findAll()).thenReturn(List.of(sampleRestaurant, second));

        // When
        List<RestaurantResponseDTO> response = restaurantService.getAll();

        // Then
        assertThat(response).hasSize(2);
        assertThat(response.get(0).getName()).isEqualTo("La Pizzeria");
        assertThat(response.get(1).getName()).isEqualTo("El Burger");
    }

    @Test
    @DisplayName("getAll: should return empty list when no restaurants exist")
    void getAll_shouldReturnEmptyList_whenNoRestaurantsExist() {
        // Given
        when(restaurantRepository.findAll()).thenReturn(List.of());

        // When
        List<RestaurantResponseDTO> response = restaurantService.getAll();

        // Then
        assertThat(response).isEmpty();
    }

    // ═══════════════════════════════════════════
    //  getByOwnerId()
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("getByOwnerId: should return restaurants for given owner")
    void getByOwnerId_shouldReturnRestaurants() {
        // Given
        when(restaurantRepository.findByOwnerId(10L)).thenReturn(List.of(sampleRestaurant));

        // When
        List<RestaurantResponseDTO> response = restaurantService.getByOwnerId(10L);

        // Then
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getOwnerId()).isEqualTo(10L);
    }

    // ═══════════════════════════════════════════
    //  update()
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("update: should update fields and return updated restaurant")
    void update_shouldUpdateAndReturn() {
        // Given
        RestaurantRequestDTO updateRequest = new RestaurantRequestDTO(
                "La Pizzeria Renovada",
                "New description",
                "Av. Corrientes 9999",
                "011-9999-0000",
                10L
        );

        Restaurant updated = Restaurant.builder()
                .id(1L)
                .name("La Pizzeria Renovada")
                .description("New description")
                .address("Av. Corrientes 9999")
                .phone("011-9999-0000")
                .ownerId(10L)
                .status(RestaurantStatus.OPEN)
                .rating(0.0)
                .build();

        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(sampleRestaurant));
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(updated);

        // When
        RestaurantResponseDTO response = restaurantService.update(1L, updateRequest);

        // Then
        assertThat(response.getName()).isEqualTo("La Pizzeria Renovada");
        assertThat(response.getAddress()).isEqualTo("Av. Corrientes 9999");

        verify(restaurantRepository, times(1)).findById(1L);
        verify(restaurantRepository, times(1)).save(any(Restaurant.class));
    }

    @Test
    @DisplayName("update: should throw when restaurant not found")
    void update_shouldThrow_whenNotFound() {
        // Given
        when(restaurantRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> restaurantService.update(99L, sampleRequest))
                .isInstanceOf(RestaurantNotFoundException.class);

        verify(restaurantRepository, never()).save(any());
    }

    // ═══════════════════════════════════════════
    //  delete()
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("delete: should delete restaurant when found")
    void delete_shouldDelete_whenFound() {
        // Given
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(sampleRestaurant));

        // When
        restaurantService.delete(1L);

        // Then
        verify(restaurantRepository, times(1)).delete(sampleRestaurant);
    }

    @Test
    @DisplayName("delete: should throw when restaurant not found")
    void delete_shouldThrow_whenNotFound() {
        // Given
        when(restaurantRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> restaurantService.delete(99L))
                .isInstanceOf(RestaurantNotFoundException.class);

        verify(restaurantRepository, never()).delete(any());
    }
}