package com.onlinemart.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onlinemart.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
