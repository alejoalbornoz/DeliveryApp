package org.deliveryapp.notification_service.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.deliveryapp.notification_service.dto.request.NotificationRequestDTO;
import org.deliveryapp.notification_service.dto.response.NotificationResponseDTO;
import org.deliveryapp.notification_service.model.Notification;
import org.deliveryapp.notification_service.model.enums.NotificationType;
import org.deliveryapp.notification_service.repository.INotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService{

    private final INotificationRepository notificationRepository;



    @Override
    @Transactional
    public NotificationResponseDTO send(NotificationRequestDTO request) {

        NotificationType type;

        try{
            type = NotificationType.valueOf(request.getType());
        }catch (IllegalArgumentException e){
            log.warn("Unknown notification type '{}', defaulting to GENERIC", request.getType());
            type = NotificationType.GENERIC;
        }

        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .type(type)
                .message(request.getMessage())
                .build();

        Notification saved = notificationRepository.save(notification);

        boolean delivered = attemptDelivery(saved);

        if (delivered) {
            saved.setSent(true);
            notificationRepository.save(saved);
        }

        log.info("Notification processed: id={}, userId={}, type={}, sent={}",
                saved.getId(), saved.getUserId(), saved.getType(), saved.isSent());

        return toResponse(saved);
    }

    @Override
    public List<NotificationResponseDTO> getByUserId(Long userId) {
        return notificationRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }


    private boolean attemptDelivery(Notification notification) {
        log.info("[NOTIFICATION STUB] To userId={} | [{}] {}",
                notification.getUserId(),
                notification.getType(),
                notification.getMessage());
        return true;
    }

    private NotificationResponseDTO toResponse(Notification notification) {
        return NotificationResponseDTO.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .message(notification.getMessage())
                .sent(notification.isSent())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
