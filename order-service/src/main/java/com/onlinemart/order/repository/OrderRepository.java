package com.onlinemart.order.repository;

import com.onlinemart.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository
        extends JpaRepository<Order, Long>,
        JpaSpecificationExecutor<Order> {
}