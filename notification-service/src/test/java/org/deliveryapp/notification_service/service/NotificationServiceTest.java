package org.deliveryapp.notification_service.service;

import org.deliveryapp.notification_service.dto.request.NotificationRequestDTO;
import org.deliveryapp.notification_service.dto.response.NotificationResponseDTO;
import org.deliveryapp.notification_service.model.Notification;
import org.deliveryapp.notification_service.model.enums.NotificationType;
import org.deliveryapp.notification_service.repository.INotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Unit Tests")
class NotificationServiceTest {

    @Mock
    private INotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Notification sampleNotification;

    @BeforeEach
    void setUp() {
        sampleNotification = Notification.builder()
                .id(1L)
                .userId(42L)
                .type(NotificationType.DRIVER_ASSIGNED)
                .message("Your driver is on the way")
                .sent(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ═══════════════════════════════════════════
    //  send()
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("send: should persist and return notification with known type")
    void send_shouldPersistAndReturn_withKnownType() {
        // Given
        NotificationRequestDTO request = new NotificationRequestDTO(
                42L, "DRIVER_ASSIGNED", "Your driver is on the way");

        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(sampleNotification);

        // When
        NotificationResponseDTO response = notificationService.send(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(42L);
        assertThat(response.getType()).isEqualTo(NotificationType.DRIVER_ASSIGNED);
        assertThat(response.isSent()).isTrue();

        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    @DisplayName("send: should fallback to GENERIC when type is unknown")
    void send_shouldFallbackToGeneric_whenTypeIsUnknown() {
        // Given
        NotificationRequestDTO request = new NotificationRequestDTO(
                42L, "UNKNOWN_TYPE_XYZ", "Some message");

        Notification genericNotification = Notification.builder()
                .id(2L)
                .userId(42L)
                .type(NotificationType.GENERIC)
                .message("Some message")
                .sent(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(genericNotification);

        // When
        NotificationResponseDTO response = notificationService.send(request);

        // Then — unknown type must not crash, must fallback to GENERIC
        assertThat(response).isNotNull();
        assertThat(response.getType()).isEqualTo(NotificationType.GENERIC);

        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    @DisplayName("send: should persist notification before attempting delivery")
    void send_shouldPersistFirst_beforeAttemptingDelivery() {
        // Given
        NotificationRequestDTO request = new NotificationRequestDTO(
                42L, "ORDER_DELIVERED", "Your order arrived!");

        Notification delivered = Notification.builder()
                .id(3L)
                .userId(42L)
                .type(NotificationType.ORDER_DELIVERED)
                .message("Your order arrived!")
                .sent(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(delivered);

        // When
        notificationService.send(request);

        // Then — save must be called at least once (persist) before marking as sent
        verify(notificationRepository, atLeast(1)).save(any(Notification.class));
    }

    // ═══════════════════════════════════════════
    //  getByUserId()
    // ═══════════════════════════════════════════

    @Test
    @DisplayName("getByUserId: should return all notifications for user")
    void getByUserId_shouldReturnAllNotifications() {
        // Given
        Notification second = Notification.builder()
                .id(2L)
                .userId(42L)
                .type(NotificationType.ORDER_CONFIRMED)
                .message("Order confirmed")
                .sent(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(notificationRepository.findByUserId(42L))
                .thenReturn(List.of(sampleNotification, second));

        // When
        List<NotificationResponseDTO> response = notificationService.getByUserId(42L);

        // Then
        assertThat(response).hasSize(2);
        assertThat(response.get(0).getType()).isEqualTo(NotificationType.DRIVER_ASSIGNED);
        assertThat(response.get(1).getType()).isEqualTo(NotificationType.ORDER_CONFIRMED);
    }

    @Test
    @DisplayName("getByUserId: should return empty list when user has no notifications")
    void getByUserId_shouldReturnEmptyList_whenNoNotifications() {
        // Given
        when(notificationRepository.findByUserId(99L)).thenReturn(List.of());

        // When
        List<NotificationResponseDTO> response = notificationService.getByUserId(99L);

        // Then
        assertThat(response).isEmpty();
    }
}