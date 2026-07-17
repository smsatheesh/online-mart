package com.onlinemart.customer.repository;

import com.onlinemart.customer.entity.CustomerDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerDetailRepository
    extends JpaRepository<CustomerDetails, Long>,
        JpaSpecificationExecutor<CustomerDetails> {

    CustomerDetails findByCustomerId(Long customerId);
}