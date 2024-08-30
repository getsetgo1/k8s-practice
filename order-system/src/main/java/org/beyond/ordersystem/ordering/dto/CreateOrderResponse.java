package org.beyond.ordersystem.ordering.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.beyond.ordersystem.ordering.domain.OrderStatus;
import org.beyond.ordersystem.ordering.domain.Ordering;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderResponse {
    private Long id;
    private String memberEmail;
    private OrderStatus orderStatus;
    private Long memberId;
    private List<CreateOrderDetailResponse> orderDetailDtos;

    public static CreateOrderResponse fromEntity(Ordering order) {
        List<CreateOrderDetailResponse> orderDetailResponseList = order.getOrderDetails().stream()
                .map(CreateOrderDetailResponse::fromEntity)
                .collect(Collectors.toList());

        return CreateOrderResponse.builder()
                .id(order.getId())
                .memberEmail(order.getMember().getEmail())
                .memberId(order.getMember().getId())
                .orderStatus(order.getOrderStatus())
                .orderDetailDtos(orderDetailResponseList)
                .build();
    }

}
