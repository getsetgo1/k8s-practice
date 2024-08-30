package org.beyond.ordersystem.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.beyond.ordersystem.common.domain.Address;
import org.beyond.ordersystem.member.domain.Member;
import org.beyond.ordersystem.member.domain.Role;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import static org.beyond.ordersystem.member.domain.Role.USER;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateMemberRequest {
    private String name;
    @NotEmpty(message = "email is essential")
    private String email;
//    @NotEmpty(message = "password is essential")
    @Size(min = 8, message = "password minimum length is 8")
    private String password;
//    private String city;
//    private String street;
//    private String zipcode;
//    private Role role;
    private Address address;
    private Role role = Role.USER; // admin 계정 생성을 위해서 추가함

    public static Member toEntity(CreateMemberRequest createMemberRequest,
                                  PasswordEncoder passwordEncoder) {
        String encodedPassword = passwordEncoder.encode(createMemberRequest.getPassword());
//        Address address = Address.builder()
//                .city(createMemberRequest.getCity())
//                .street(createMemberRequest.getStreet())
//                .zipcode(createMemberRequest.getZipcode())
//                .build();

        return Member.builder()
                .name(createMemberRequest.getName())
                .email(createMemberRequest.getEmail())
                .password(encodedPassword)
//                .password(createMemberRequest.getPassword())
                .address(createMemberRequest.getAddress())
                .role(createMemberRequest.getRole())
//                .role(USER) // @Builder.Default로 대체
                .build();
    }
}
