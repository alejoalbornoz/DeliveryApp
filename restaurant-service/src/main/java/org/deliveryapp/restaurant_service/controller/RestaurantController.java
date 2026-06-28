package org.deliveryapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.deliveryapp.dto.request.RestaurantRequestDTO;
import org.deliveryapp.dto.response.RestaurantResponseDTO;
import org.deliveryapp.service.RestaurantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
@Tag(name = "Restaurants", description = "Manage restaurant listings")
public class RestaurantController {

    private final RestaurantService restaurantService;

    @PostMapping
    @Operation(summary = "Create a new restaurant")
    public ResponseEntity<RestaurantResponseDTO> create(
            @Valid @RequestBody RestaurantRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurantService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a restaurant by ID")
    public ResponseEntity<RestaurantResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getById(id));
    }

    @GetMapping
    @Operation(summary = "List all restaurants")
    public ResponseEntity<List<RestaurantResponseDTO>> getAll() {
        return ResponseEntity.ok(restaurantService.getAll());
    }

    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "List restaurants owned by a specific user")
    public ResponseEntity<List<RestaurantResponseDTO>> getByOwnerId(@PathVariable Long ownerId) {
        return ResponseEntity.ok(restaurantService.getByOwnerId(ownerId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a restaurant")
    public ResponseEntity<RestaurantResponseDTO> update(
            @PathVariable Long id, @Valid @RequestBody RestaurantRequestDTO request) {
        return ResponseEntity.ok(restaurantService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a restaurant")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        restaurantService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
