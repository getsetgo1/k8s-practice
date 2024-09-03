package org.beyond.ordersystem.ordering.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.beyond.ordersystem.ordering.domain.Ordering;

import java.util.List;

import static org.beyond.ordersystem.ordering.domain.OrderStatus.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateOrderRequest {
//    private List<CreateOrderDetailRequest> orderDetailList;

    private Long productId;
    private Integer quantity;

    public static Ordering toEntity(String memberEmail) {
        return Ordering.builder()
                .memberEmail(memberEmail)
//                .orderStatus(ORDERED) // @Builder.Default로 대체
                .build();
    }
}
