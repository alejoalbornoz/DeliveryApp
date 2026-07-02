package org.deliveryapp.notification_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.deliveryapp.notification_service.model.enums.NotificationType;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDTO {

    private Long id;
    private Long userId;
    private NotificationType type;
    private String message;
    private boolean sent;
    private LocalDateTime createdAt;
}
