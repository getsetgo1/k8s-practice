package org.beyond.ordersystem.ordering.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.beyond.ordersystem.ordering.domain.OrderDetail;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderDetailResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Integer count;

    public static CreateOrderDetailResponse fromEntity(OrderDetail orderDetail) {
        return CreateOrderDetailResponse.builder()
                .id(orderDetail.getId())
                .productId(orderDetail.getProduct().getId())
                .productName(orderDetail.getProduct().getName())
                .count(orderDetail.getQuantity())
                .build();
    }
}
