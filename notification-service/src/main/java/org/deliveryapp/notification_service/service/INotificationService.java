package org.deliveryapp.notification_service.service;


import org.deliveryapp.notification_service.dto.request.NotificationRequestDTO;
import org.deliveryapp.notification_service.dto.response.NotificationResponseDTO;

import java.util.List;

public interface INotificationService {

    NotificationResponseDTO send(NotificationRequestDTO request);

    List<NotificationResponseDTO> getByUserId(Long userId);

}
