package org.beyond.ordersystem.ordering.dto;

import lombok.*;
import org.beyond.ordersystem.ordering.domain.OrderDetail;
import org.beyond.ordersystem.ordering.domain.Ordering;
import org.beyond.ordersystem.product.domain.Product;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateOrderDetailRequest {
    private Long productId;
    private Integer quantity;

    public static OrderDetail toEntity(CreateOrderRequest createOrderRequest, Product product, Ordering order) {
        return OrderDetail.builder()
                .quantity(createOrderRequest.getQuantity())
                .ordering(order)
                .product(product)
                .build();
    }
}
