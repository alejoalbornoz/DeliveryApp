package org.deliveryapp.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.deliveryapp.dto.request.MenuItemRequestDTO;
import org.deliveryapp.dto.response.MenuItemResponseDTO;
import org.deliveryapp.service.MenuService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/menu")
@RequiredArgsConstructor
@Tag(name = "Menu", description = "Manage restaurant menu items")
public class MenuController {

    private final MenuService menuService;

    @PostMapping
    @Operation(summary = "Create a new menu item")
    public ResponseEntity<MenuItemResponseDTO> create(
            @Valid @RequestBody MenuItemRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a menu item by ID — also used by order-service via Feign")
    public ResponseEntity<MenuItemResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(menuService.getById(id));
    }

    @GetMapping("/restaurant/{restaurantId}")
    @Operation(summary = "List all menu items for a restaurant")
    public ResponseEntity<List<MenuItemResponseDTO>> getByRestaurantId(
            @PathVariable Long restaurantId) {
        return ResponseEntity.ok(menuService.getByRestaurantId(restaurantId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a menu item")
    public ResponseEntity<MenuItemResponseDTO> update(
            @PathVariable Long id, @Valid @RequestBody MenuItemRequestDTO request) {
        return ResponseEntity.ok(menuService.update(id, request));
    }

    @PatchMapping("/{id}/availability")
    @Operation(summary = "Toggle a menu item's availability (in/out of stock)")
    public ResponseEntity<MenuItemResponseDTO> toggleAvailability(@PathVariable Long id) {
        return ResponseEntity.ok(menuService.toggleAvailability(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a menu item")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
