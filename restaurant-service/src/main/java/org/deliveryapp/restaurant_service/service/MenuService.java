package org.deliveryapp.restaurant_service.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.deliveryapp.restaurant_service.exception.MenuItemNotFoundException;
import org.deliveryapp.restaurant_service.dto.request.MenuItemRequestDTO;
import org.deliveryapp.restaurant_service.dto.response.MenuItemResponseDTO;
import org.deliveryapp.restaurant_service.exception.CategoryNotFoundException;
import org.deliveryapp.restaurant_service.exception.RestaurantNotFoundException;
import org.deliveryapp.restaurant_service.model.Category;
import org.deliveryapp.restaurant_service.model.MenuItem;
import org.deliveryapp.restaurant_service.model.Restaurant;
import org.deliveryapp.restaurant_service.repository.ICategoryRepository;
import org.deliveryapp.restaurant_service.repository.IMenuItemRepository;
import org.deliveryapp.restaurant_service.repository.IRestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService implements IMenuService{

    private final IMenuItemRepository menuItemRepository;
    private final IRestaurantRepository restaurantRepository;
    private final ICategoryRepository categoryRepository;

    @Override
    @Transactional
    public MenuItemResponseDTO create(MenuItemRequestDTO request) {
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException(request.getRestaurantId()));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(request.getCategoryId()));

        MenuItem menuItem = MenuItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .restaurant(restaurant)
                .category(category)
                .build();

        MenuItem saved = menuItemRepository.save(menuItem);
        log.info("Menu item created: id={}, restaurantId={}", saved.getId(), restaurant.getId());
        return toResponse(saved);
    }

    @Override
    public MenuItemResponseDTO getById(Long id) {
        return toResponse(findMenuItemOrThrow(id));
    }

    @Override
    public List<MenuItemResponseDTO> getByRestaurantId(Long restaurantId) {
        return menuItemRepository.findByRestaurantId(restaurantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public MenuItemResponseDTO update(Long id, MenuItemRequestDTO request) {
        MenuItem menuItem = findMenuItemOrThrow(id);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(request.getCategoryId()));

        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setCategory(category);
        // restaurant is intentionally not reassignable through a plain update —
        // moving a menu item to a different restaurant is not a supported operation.

        MenuItem updated = menuItemRepository.save(menuItem);
        log.info("Menu item updated: id={}", updated.getId());
        return toResponse(updated);
    }

    @Override
    @Transactional
    public MenuItemResponseDTO toggleAvailability(Long id) {
        MenuItem menuItem = findMenuItemOrThrow(id);
        menuItem.setAvailable(!menuItem.isAvailable());
        MenuItem updated = menuItemRepository.save(menuItem);
        log.info("Menu item availability toggled: id={}, available={}",
                updated.getId(), updated.isAvailable());
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        MenuItem menuItem = findMenuItemOrThrow(id);
        menuItemRepository.delete(menuItem);
        log.info("Menu item deleted: id={}", id);
    }

    private MenuItem findMenuItemOrThrow(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new MenuItemNotFoundException(id));
    }

    private MenuItemResponseDTO toResponse(MenuItem menuItem) {
        return MenuItemResponseDTO.builder()
                .id(menuItem.getId())
                .name(menuItem.getName())
                .description(menuItem.getDescription())
                .price(menuItem.getPrice())
                .available(menuItem.isAvailable())
                .restaurantId(menuItem.getRestaurant().getId())
                .categoryName(menuItem.getCategory().getName())
                .build();
    }
}
