package org.deliveryapp.order_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponseDTO {

    private Long id;
    private Long menuItemId;
    private String menuItemName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
}
