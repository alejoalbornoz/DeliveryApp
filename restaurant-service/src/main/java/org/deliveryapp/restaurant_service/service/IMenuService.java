package org.deliveryapp.restaurant_service.service;



import org.deliveryapp.restaurant_service.dto.request.MenuItemRequestDTO;
import org.deliveryapp.restaurant_service.dto.response.MenuItemResponseDTO;

import java.util.List;

public interface IMenuService {

    MenuItemResponseDTO create(MenuItemRequestDTO request);

    MenuItemResponseDTO getById(Long id);

    List<MenuItemResponseDTO> getByRestaurantId(Long restaurantId);

    MenuItemResponseDTO update(Long id, MenuItemRequestDTO request);

    MenuItemResponseDTO toggleAvailability(Long id);

    void delete(Long id);
}
