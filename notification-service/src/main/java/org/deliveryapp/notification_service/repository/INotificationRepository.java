package org.deliveryapp.notification_service.repository;

import org.deliveryapp.notification_service.model.Notification;
import org.deliveryapp.notification_service.model.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface INotificationRepository extends JpaRepository <Notification, Long> {

    List<Notification> findByUserId (Long userId);

    List<Notification> findByUserIdAndType(Long userId, NotificationType type);

    List<Notification> findBySentFalse();

}
