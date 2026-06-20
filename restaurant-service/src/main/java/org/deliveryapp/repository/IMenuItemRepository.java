package org.deliveryapp.repository;

import org.deliveryapp.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IMenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByRestaurantId(Long restaurantId);
    List<MenuItem> findByRestaurantIdAndAvailableTrue(Long restaurantId);
    List<MenuItem> findByCategoryId(Long categoryId);

}
