package org.beyond.ordersystem.member.repository;

import org.beyond.ordersystem.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Page<Member> findAll(Pageable pageable);

    Optional<Member> findByEmail(String email);

    default Member findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException("회원이 없습니다."));
    }
}
