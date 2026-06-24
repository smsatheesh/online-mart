package com.onlinemart.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onlinemart.order.entity.OrderItems;

public interface OrderItemRepository extends JpaRepository<OrderItems, Long> {
}
