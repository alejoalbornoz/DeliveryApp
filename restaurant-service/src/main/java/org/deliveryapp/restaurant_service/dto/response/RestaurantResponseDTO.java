package org.deliveryapp.restaurant_service.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.deliveryapp.restaurant_service.model.enums.RestaurantStatus;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantResponseDTO {

    private Long id;
    private String name;
    private String description;
    private String address;
    private String phone;
    private Long ownerId;
    private RestaurantStatus status;
    private Double rating;
}
