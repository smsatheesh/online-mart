package com.onlinemart.customer.service;

import com.onlinemart.customer.dto.request.customer.CreateCustomerDto;
import com.onlinemart.customer.dto.response.customer.CustomerResponseDto;

public interface CustomerService {

    CustomerResponseDto saveCustomer(CreateCustomerDto createCustomerDto);

    CustomerResponseDto fetchCustomer(Long customerId);

}