package org.beyond.ordersystem.ordering.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.beyond.ordersystem.member.domain.Member;
import org.beyond.ordersystem.ordering.domain.OrderDetail;
import org.beyond.ordersystem.ordering.domain.OrderStatus;
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

    public static Ordering toEntity(Member member) {
        return Ordering.builder()
                .member(member)
//                .orderStatus(ORDERED) // @Builder.Default로 대체
                .build();
    }
}
