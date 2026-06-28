package org.deliveryapp.dto.response;

import org.deliveryapp.model.enums.RestaurantStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
