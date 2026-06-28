package org.deliveryapp.service;

import org.deliveryapp.dto.request.MenuItemRequestDTO;
import org.deliveryapp.dto.response.MenuItemResponseDTO;

import java.util.List;

public interface IMenuService {

    MenuItemResponseDTO create(MenuItemRequestDTO request);

    MenuItemResponseDTO getById(Long id);

    List<MenuItemResponseDTO> getByRestaurantId(Long restaurantId);

    MenuItemResponseDTO update(Long id, MenuItemRequestDTO request);

    MenuItemResponseDTO toggleAvailability(Long id);

    void delete(Long id);
}
