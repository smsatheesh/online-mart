package com.onlinemart.cart.repository;

import com.onlinemart.cart.entity.Cart;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository
        extends JpaRepository<Cart, Long>,
        JpaSpecificationExecutor<Cart> {
    Cart findByCustomerIdAndPlatform(Long customerId, String platform);

    List<Cart> findByCustomerId(Long customerId);
}