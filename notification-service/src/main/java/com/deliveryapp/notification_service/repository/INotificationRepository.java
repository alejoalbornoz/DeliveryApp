package com.deliveryapp.notification_service.repository;

import com.deliveryapp.notification_service.model.Notification;
import com.deliveryapp.notification_service.model.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface INotificationRepository extends JpaRepository <Notification, Long> {

    List<Notification> findByUserId (Long userId);

    List<Notification> findByUserAndType(Long userId, NotificationType type);

    List<Notification> findBySentFalse();

}
