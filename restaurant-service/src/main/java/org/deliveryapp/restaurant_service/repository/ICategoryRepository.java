package org.deliveryapp.restaurant_service.repository;



import org.deliveryapp.restaurant_service.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface ICategoryRepository extends JpaRepository <Category, Long> {
    Optional<Category> findByName(String name);

    boolean existByName(String name);
}
