package org.deliveryapp.restaurant_service.service;



import org.deliveryapp.restaurant_service.dto.request.RestaurantRequestDTO;
import org.deliveryapp.restaurant_service.dto.response.RestaurantResponseDTO;

import java.util.List;

public interface IRestaurantService {
    RestaurantResponseDTO create(RestaurantRequestDTO request);

    RestaurantResponseDTO getById(Long id);

    List<RestaurantResponseDTO> getAll();

    List<RestaurantResponseDTO> getByOwnerId(Long ownerId);

    RestaurantResponseDTO update(Long id, RestaurantRequestDTO request);

    void delete(Long id);
}
