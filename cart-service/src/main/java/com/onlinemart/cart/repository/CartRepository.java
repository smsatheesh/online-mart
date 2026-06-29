package com.onlinemart.cart.repository;

import com.onlinemart.cart.entity.Cart;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository
        extends JpaRepository<Cart, Long>,
        JpaSpecificationExecutor<Cart> {
    Optional<Cart> findByCustomerId(Long customerId);
}