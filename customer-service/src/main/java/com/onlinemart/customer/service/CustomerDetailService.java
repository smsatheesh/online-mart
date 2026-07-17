package com.onlinemart.customer.service;

import com.onlinemart.customer.dto.request.customerDetails.SaveCustomerDetailsDto;
import com.onlinemart.customer.dto.response.customerDetails.UpdateCustomerDetailsResponseDto;

public interface CustomerDetailService {

    void deactivateCustomer(Long customerId);

    void resetPassword(Long customerId, String password);

    UpdateCustomerDetailsResponseDto updateCustomerDetails(SaveCustomerDetailsDto requestDto);

}
