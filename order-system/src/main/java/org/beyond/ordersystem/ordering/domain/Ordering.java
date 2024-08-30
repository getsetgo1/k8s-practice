package org.beyond.ordersystem.ordering.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.beyond.ordersystem.member.domain.Member;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Ordering {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus = OrderStatus.ORDERED;

    @Builder.Default // 빌더 패턴에서도 ArrayList로 초기화 되도록
    @OneToMany(mappedBy = "ordering", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private List<OrderDetail> orderDetails = new ArrayList<>();

    public void updateDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails ;
    }

    public void updateStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}
