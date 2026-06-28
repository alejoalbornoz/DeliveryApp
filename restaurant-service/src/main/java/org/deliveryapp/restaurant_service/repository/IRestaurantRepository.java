package org.deliveryapp.repository;

import org.deliveryapp.model.Restaurant;
import org.deliveryapp.model.enums.RestaurantStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IRestaurantRepository extends JpaRepository <Restaurant, Long>{

    List<Restaurant> findByStatus(RestaurantStatus status);
    List<Restaurant> findByOwnerId(Long ownerId);
    boolean existsByNameAndOwnerId(String name, Long ownerId);
}
