package com.onlinemart.cart.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onlinemart.cart.entity.CartItems;

public interface CartItemRepository extends JpaRepository<CartItems, Long> {

    List<CartItems> findByCartId(Long cartId);

    void deleteAllByCartId(Long cartId);
}