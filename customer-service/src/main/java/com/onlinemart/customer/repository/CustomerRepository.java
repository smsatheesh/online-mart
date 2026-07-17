package com.onlinemart.customer.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.onlinemart.customer.entity.Customer;

@Repository
public interface CustomerRepository
    extends JpaRepository<Customer, Long>,
        JpaSpecificationExecutor<Customer> {
}