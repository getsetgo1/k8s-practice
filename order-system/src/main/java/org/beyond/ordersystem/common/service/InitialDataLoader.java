package org.beyond.ordersystem.common.service;

import lombok.RequiredArgsConstructor;
import org.beyond.ordersystem.common.domain.Address;
import org.beyond.ordersystem.member.domain.Role;
import org.beyond.ordersystem.member.dto.CreateMemberRequest;
import org.beyond.ordersystem.member.repository.MemberRepository;
import org.beyond.ordersystem.member.service.MemberService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// CommandLineRunner를 상속함으로서 해당 컴포넌트가 스프링 빈으로 등록되는 시점에 run 메서드가 실행된다.
@RequiredArgsConstructor
@Component
public class InitialDataLoader implements CommandLineRunner {
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    @Override
    public void run(String... args) throws Exception {
        System.out.println("hello world!");
        String email = "admin@test.com";

        if( memberRepository.findByEmail(email).isEmpty()) {
            CreateMemberRequest admin = CreateMemberRequest.builder()
                    .name("admin")
                    .email("admin@test.com")
                    .password("12341234")
                    .role(Role.ADMIN)
                    .address(new Address())
                    .build();

            memberService.createMember(admin);
        }

    }
}
