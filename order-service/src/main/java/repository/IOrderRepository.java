package repository;

import model.Order;
import model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IOrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    List<Order> findByRestaurantId(Long restaurantId);

    List<Order> findByStatus(OrderStatus status);

}
