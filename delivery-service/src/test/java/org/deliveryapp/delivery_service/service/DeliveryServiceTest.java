package org.deliveryapp.delivery_service.service;

import org.deliveryapp.delivery_service.client.NotificationClient;
import org.deliveryapp.delivery_service.dto.request.DeliveryRequestDTO;
import org.deliveryapp.delivery_service.dto.response.DeliveryResponseDTO;
import org.deliveryapp.delivery_service.event.DeliveryEventProducer;
import org.deliveryapp.delivery_service.exception.DeliveryNotFoundException;
import org.deliveryapp.delivery_service.exception.NoAvailableDriverException;
import org.deliveryapp.delivery_service.model.Delivery;
import org.deliveryapp.delivery_service.model.Driver;
import org.deliveryapp.delivery_service.model.enums.DeliveryStatus;
import org.deliveryapp.delivery_service.model.enums.DriverStatus;
import org.deliveryapp.delivery_service.repository.IDeliveryRepository;
import org.deliveryapp.delivery_service.repository.IDriverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeliveryService Unit Tests")
class DeliveryServiceTest {

    @Mock
    private IDeliveryRepository deliveryRepository;

    @Mock
    private IDriverRepository driverRepository;

    @Mock
    private DeliveryEventProducer deliveryEventProducer;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private DeliveryService deliveryService;

    private Delivery sampleDelivery;
    private Driver sampleDriver;
    private DeliveryRequestDTO sampleRequest;

    @BeforeEach
    void setUp() {
        sampleDriver = Driver.builder()
                .id(1L)
                .userId(100L)
                .name("Carlos Repartidor")
                .phone("011-1111-2222")
                .vehiclePlate("ABC123")
                .status(DriverStatus.AVAILABLE)
                .build();

        sampleDelivery = Delivery.builder()
                .id(1L)
                .orderId(42L)
                .deliveryAddress("Av. Corrientes 1234")
                .status(DeliveryStatus.PENDING_ASSIGNMENT)
                .build();

        sampleRequest = new DeliveryRequestDTO(42L, "Av. Corrientes 1234");
    }

    // ═══════════════════════════════════════════
    //  createDelivery()
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("createDelivery: should save and return delivery")
    void createDelivery_shouldSaveAndReturn() {
        // Given
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(sampleDelivery);

        // When
        DeliveryResponseDTO response = deliveryService.createDelivery(sampleRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(42L);
        assertThat(response.getStatus()).isEqualTo(DeliveryStatus.PENDING_ASSIGNMENT);

        verify(deliveryRepository, times(1)).save(any(Delivery.class));
    }

    // ═══════════════════════════════════════════
    //  getById()
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("getById: should return delivery when found")
    void getById_shouldReturn_whenFound() {
        // Given
        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(sampleDelivery));

        // When
        DeliveryResponseDTO response = deliveryService.getById(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getById: should throw DeliveryNotFoundException when not found")
    void getById_shouldThrow_whenNotFound() {
        // Given
        when(deliveryRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> deliveryService.getById(99L))
                .isInstanceOf(DeliveryNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ═══════════════════════════════════════════
    //  assignDriver()
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("assignDriver: should assign available driver and send notification")
    void assignDriver_shouldAssignDriver_andSendNotification() {
        // Given
        doNothing().when(deliveryEventProducer).publishDeliveryStatus(any());
        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(sampleDelivery));
        when(driverRepository.findFirstByStatus(DriverStatus.AVAILABLE))
                .thenReturn(Optional.of(sampleDriver));
        when(driverRepository.save(any(Driver.class))).thenReturn(sampleDriver);

        Delivery assigned = Delivery.builder()
                .id(1L)
                .orderId(42L)
                .driver(sampleDriver)
                .deliveryAddress("Av. Corrientes 1234")
                .status(DeliveryStatus.ASSIGNED)
                .build();

        when(deliveryRepository.save(any(Delivery.class))).thenReturn(assigned);

        // When
        DeliveryResponseDTO response = deliveryService.assignDriver(1L);

        // Then
        assertThat(response.getStatus()).isEqualTo(DeliveryStatus.ASSIGNED);
        assertThat(response.getDriverId()).isEqualTo(1L);

        verify(driverRepository, times(1)).save(any(Driver.class));
    }

    @Test
    @DisplayName("assignDriver: should throw NoAvailableDriverException when no drivers available")
    void assignDriver_shouldThrow_whenNoDriverAvailable() {
        // Given
        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(sampleDelivery));
        when(driverRepository.findFirstByStatus(DriverStatus.AVAILABLE))
                .thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> deliveryService.assignDriver(1L))
                .isInstanceOf(NoAvailableDriverException.class);

        // Driver and delivery should not be saved
        verify(driverRepository, never()).save(any());
        verify(deliveryRepository, never()).save(any());
    }

    @Test
    @DisplayName("assignDriver: should throw DeliveryNotFoundException when delivery not found")
    void assignDriver_shouldThrow_whenDeliveryNotFound() {
        // Given
        when(deliveryRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> deliveryService.assignDriver(99L))
                .isInstanceOf(DeliveryNotFoundException.class);
    }

    // ═══════════════════════════════════════════
    //  updateStatus()
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("updateStatus: should update status to IN_TRANSIT")
    void updateStatus_shouldUpdateStatus() {
        // Given
        Delivery inTransit = Delivery.builder()
                .id(1L)
                .orderId(42L)
                .deliveryAddress("Av. Corrientes 1234")
                .status(DeliveryStatus.IN_TRANSIT)
                .build();

        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(sampleDelivery));
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(inTransit);

        // When
        DeliveryResponseDTO response = deliveryService.updateStatus(1L, DeliveryStatus.IN_TRANSIT);

        // Then
        assertThat(response.getStatus()).isEqualTo(DeliveryStatus.IN_TRANSIT);
        verify(deliveryRepository, times(1)).save(any(Delivery.class));
    }

    @Test
    @DisplayName("updateStatus: should free driver when status is DELIVERED")
    void updateStatus_shouldFreeDriver_whenDelivered() {
        // Given - delivery already has a driver assigned
        doNothing().when(deliveryEventProducer).publishDeliveryStatus(any());
        Delivery deliveryWithDriver = Delivery.builder()
                .id(1L)
                .orderId(42L)
                .driver(sampleDriver)
                .deliveryAddress("Av. Corrientes 1234")
                .status(DeliveryStatus.IN_TRANSIT)
                .build();

        Delivery delivered = Delivery.builder()
                .id(1L)
                .orderId(42L)
                .driver(sampleDriver)
                .deliveryAddress("Av. Corrientes 1234")
                .status(DeliveryStatus.DELIVERED)
                .build();

        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(deliveryWithDriver));
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(delivered);

        // When
        deliveryService.updateStatus(1L, DeliveryStatus.DELIVERED);

        // Then — driver should be freed (status set to AVAILABLE)
        verify(driverRepository, times(1)).save(any(Driver.class));
    }
}