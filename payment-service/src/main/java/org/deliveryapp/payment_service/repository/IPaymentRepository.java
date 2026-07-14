package org.deliveryapp.payment_service.repository;

import org.deliveryapp.payment_service.model.Payment;
import org.deliveryapp.payment_service.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IPaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);
    Optional<Payment> findByMpPaymentId(String mpPaymentId);
    List<Payment> findByStatus(PaymentStatus status);


}
