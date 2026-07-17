package com.onlinemart.customer.mapper;

import com.onlinemart.customer.dto.request.customerAddress.CreateCustomerAddressRequestDto;
import com.onlinemart.customer.dto.response.customerAddress.CustomerAddressDto;
import com.onlinemart.customer.entity.CustomerAddress;
import org.springframework.stereotype.Component;

@Component
public class CustomerAddressMapper {
    
    public CustomerAddress toEntity(Long customerId, CreateCustomerAddressRequestDto dto) {
        if (dto == null) {
            return null;
        }

        CustomerAddress customerAddress = new CustomerAddress();
        customerAddress.setCustomerId(customerId);
        customerAddress.setAddressType(dto.getAddressType());
        customerAddress.setAddressFirstLine(dto.getAddressFirstLine());
        customerAddress.setAddressSecondLine(dto.getAddressSecondLine());
        customerAddress.setLandmark(dto.getLandmark());
        customerAddress.setPostalCode(dto.getPostalCode());
        customerAddress.setCity(dto.getCity());
        customerAddress.setState(dto.getState());
        customerAddress.setCountry(dto.getCountry());

        return customerAddress;
    }

    public CustomerAddressDto toSaveResponseDto(CustomerAddress customerAddress) {
        return CustomerAddressDto.builder()
                .addressId(customerAddress.getAddressId())
                .customerId(customerAddress.getCustomerId())
                .addressType(customerAddress.getAddressType())
                .addressFirstLine(customerAddress.getAddressFirstLine())
                .addressSecondLine(customerAddress.getAddressSecondLine())
                .landmark(customerAddress.getLandmark())
                .postalCode(customerAddress.getPostalCode())
                .city(customerAddress.getCity())
                .state(customerAddress.getCity())
                .country(customerAddress.getCountry())
                .createdBy(customerAddress.getCreatedBy())
                .createdAt(customerAddress.getCreatedAt())
                .updatedBy(customerAddress.getUpdatedBy())
                .updatedAt(customerAddress.getUpdatedAt())
                .build();

    }
}
