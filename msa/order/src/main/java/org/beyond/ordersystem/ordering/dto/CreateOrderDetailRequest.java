package org.beyond.ordersystem.ordering.dto;

import lombok.*;
import org.beyond.ordersystem.ordering.domain.OrderDetail;
import org.beyond.ordersystem.ordering.domain.Ordering;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateOrderDetailRequest {
    private Long productId;
    private Integer quantity;

    public static OrderDetail toEntity(CreateOrderRequest createOrderRequest, Long productId, Ordering order) {
        return OrderDetail.builder()
                .quantity(createOrderRequest.getQuantity())
                .ordering(order)
                .productId(productId)
                .build();
    }
}
