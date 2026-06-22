package org.deliveryapp.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.deliveryapp.repository.IRestaurantRepository;
import org.deliveryapp.dto.request.RestaurantRequestDTO;
import org.deliveryapp.dto.response.RestaurantResponseDTO;
import org.deliveryapp.exception.RestaurantNotFoundException;
import org.deliveryapp.model.Restaurant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantService implements IRestaurantService{

    private final IRestaurantRepository restaurantRepository;

    @Override
    @Transactional
    public RestaurantResponseDTO create(RestaurantRequestDTO request) {
        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .description(request.getDescription())
                .address(request.getAddress())
                .phone(request.getPhone())
                .ownerId(request.getOwnerId())
                .build();

        Restaurant saved = restaurantRepository.save(restaurant);
        log.info("Restaurant created: id={}, name={}", saved.getId(), saved.getName());
        return toResponse(saved);
    }

    @Override
    public RestaurantResponseDTO getById(Long id) {
        return toResponse(findRestaurantOrThrow(id));
    }

    @Override
    public List<RestaurantResponseDTO> getAll() {
        return restaurantRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<RestaurantResponseDTO> getByOwnerId(Long ownerId) {
        return restaurantRepository.findByOwnerId(ownerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public RestaurantResponseDTO update(Long id, RestaurantRequestDTO request) {
        Restaurant restaurant = findRestaurantOrThrow(id);

        restaurant.setName(request.getName());
        restaurant.setDescription(request.getDescription());
        restaurant.setAddress(request.getAddress());
        restaurant.setPhone(request.getPhone());
        // ownerId is intentionally not updatable here — changing restaurant
        // ownership is a separate, more sensitive operation than a basic edit.

        Restaurant updated = restaurantRepository.save(restaurant);
        log.info("Restaurant updated: id={}", updated.getId());
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Restaurant restaurant = findRestaurantOrThrow(id);
        restaurantRepository.delete(restaurant);
        log.info("Restaurant deleted: id={}", id);
    }

    private Restaurant findRestaurantOrThrow(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new RestaurantNotFoundException(id));
    }

    private RestaurantResponseDTO toResponse(Restaurant restaurant) {
        return RestaurantResponseDTO.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .description(restaurant.getDescription())
                .address(restaurant.getAddress())
                .phone(restaurant.getPhone())
                .ownerId(restaurant.getOwnerId())
                .status(restaurant.getStatus())
                .rating(restaurant.getRating())
                .build();
    }
}
