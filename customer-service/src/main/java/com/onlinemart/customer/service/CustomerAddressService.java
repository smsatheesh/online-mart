package com.onlinemart.customer.service;

import com.onlinemart.customer.dto.request.customerAddress.CreateCustomerAddressRequestDto;
import com.onlinemart.customer.dto.request.customerAddress.UpdateAddressRequestDto;
import com.onlinemart.customer.dto.response.customerAddress.CustomerAddressResponseDto;
import com.onlinemart.customer.dto.response.customerAddress.CustomerAddressesResponseDto;

public interface CustomerAddressService {

    CustomerAddressResponseDto createAddress(CreateCustomerAddressRequestDto requestDto);

    CustomerAddressResponseDto updateAddress(UpdateAddressRequestDto requestDto);

    CustomerAddressesResponseDto fetchAddresses(Long customerId);

}
