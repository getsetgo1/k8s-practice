package org.beyond.ordersystem.member.dto;

import lombok.*;
import org.beyond.ordersystem.member.domain.Member;
import org.beyond.ordersystem.member.domain.Role;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateMemberResponse {
    private Long id;
    private String name;
    private String email;
    private String password;
    private String city;
    private String street;
    private String zipcode;
    private Role role;

    public static CreateMemberResponse fromEntity(Member member) {
        return CreateMemberResponse.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .password(member.getPassword())
                .city(member.getAddress().getCity())
                .street(member.getAddress().getCity())
                .zipcode(member.getAddress().getZipcode())
                .role(member.getRole())
                .build();
    }
}
