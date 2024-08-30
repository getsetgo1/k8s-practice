package org.beyond.ordersystem.product.repository;

import org.beyond.ordersystem.product.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.persistence.EntityNotFoundException;


public interface ProductRepository extends JpaRepository<Product, Long> {
//    Page<Product> findAll(Pageable pageable);
    Page<Product> findAll(Specification<Product> specification, Pageable pageable);

    default Product findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException("상품 없음"));
    }
}
