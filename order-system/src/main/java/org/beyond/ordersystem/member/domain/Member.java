package org.beyond.ordersystem.member.domain;


import lombok.*;
import org.beyond.ordersystem.common.domain.Address;
import org.beyond.ordersystem.common.domain.BaseEntity;
import org.beyond.ordersystem.ordering.domain.Ordering;

import javax.persistence.*;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;
    private String password;

//    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @Builder.Default
    @Embedded
    private Address address = new Address();

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    List<Ordering> orderList;

    public void updateAddress(Address address) {
        this.address = address;
    }

    public void updatePassword(String password) {
        this.password = password;
    }
}
