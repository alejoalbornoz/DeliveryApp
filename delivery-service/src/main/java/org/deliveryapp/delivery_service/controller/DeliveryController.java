package org.deliveryapp.delivery_service.controller;

import feign.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.deliveryapp.delivery_service.dto.request.DeliveryRequestDTO;
import org.deliveryapp.delivery_service.dto.response.DeliveryResponseDTO;
import org.deliveryapp.delivery_service.model.enums.DeliveryStatus;
import org.deliveryapp.delivery_service.repository.IDeliveryRepository;
import org.deliveryapp.delivery_service.service.DeliveryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/delivery")
@RequiredArgsConstructor
@Tag(name = "Delivery", description = "Manage delivery assignment and tracking")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping
    @Operation(summary = "Create a delivery record for an order")
    public ResponseEntity<DeliveryResponseDTO> create(@Valid @RequestBody DeliveryRequestDTO request){
        return ResponseEntity.status(HttpStatus.CREATED).body(deliveryService.createDelivery(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a delivery by ID")
    public ResponseEntity<DeliveryResponseDTO> getById (@PathVariable Long id){
        return ResponseEntity.ok(deliveryService.getById(id));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get the delivery associated with an order")
    public ResponseEntity <DeliveryResponseDTO> getByOrderId (@PathVariable Long orderId){
        return ResponseEntity.ok(deliveryService.getByOrderId(orderId));
    }

    @PatchMapping("/{id}/assign")
    @Operation(summary = "Assign the next available driver to this delivery")
    public ResponseEntity<DeliveryResponseDTO> assignDriver(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryService.assignDriver(id));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update delivery status (picked up, in transit, delivered, etc.)")
    public ResponseEntity<DeliveryResponseDTO> updateStatus(
            @PathVariable Long id, @RequestParam DeliveryStatus status) {
        return ResponseEntity.ok(deliveryService.updateStatus(id, status));
    }



}
