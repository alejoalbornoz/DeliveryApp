package org.deliveryapp.repository;

import org.deliveryapp.model.Category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface ICategoryRepository extends JpaRepository <Category, Long> {
    Optional<Category> findByName(String name);

    boolean existByName(String name);
}
