package org.beyond.ordersystem.ordering.repository;

import org.beyond.ordersystem.member.domain.Member;
import org.beyond.ordersystem.ordering.domain.Ordering;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.persistence.EntityNotFoundException;

public interface OrderingRepository extends JpaRepository<Ordering, Long> {
    default Ordering findByIdOrThrow(Long orderingId) {
        return findById(orderingId)
                .orElseThrow(() -> new EntityNotFoundException("그런거 없어"));
    }

    Page<Ordering> findAllByMember(Pageable pageable, Member member);
}
