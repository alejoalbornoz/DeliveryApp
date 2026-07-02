package org.deliveryapp.restaurant_service.repository;


import org.deliveryapp.model.enums.RestaurantStatus;
import org.deliveryapp.restaurant_service.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IRestaurantRepository extends JpaRepository <Restaurant, Long>{

    List<Restaurant> findByStatus(RestaurantStatus status);
    List<Restaurant> findByOwnerId(Long ownerId);
    boolean existsByNameAndOwnerId(String name, Long ownerId);
}
