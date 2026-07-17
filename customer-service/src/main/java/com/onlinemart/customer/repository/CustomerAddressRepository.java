package com.onlinemart.customer.repository;

import com.onlinemart.customer.entity.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerAddressRepository
    extends JpaRepository<CustomerAddress, Long>,
        JpaSpecificationExecutor<CustomerAddress> {

    List<CustomerAddress> findByCustomerId(Long customerId);
}