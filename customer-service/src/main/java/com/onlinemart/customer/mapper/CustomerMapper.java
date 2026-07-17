package com.onlinemart.customer.mapper;

import com.onlinemart.customer.dto.request.customer.CreateCustomerDto;
import com.onlinemart.customer.dto.response.customer.SaveCustomerDataDto;
import com.onlinemart.customer.entity.Customer;
import com.onlinemart.customer.entity.CustomerDetails;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    private final CustomerDetailsMapper customerDetailsMapper;

    public CustomerMapper(CustomerDetailsMapper customerDetailsMapper) {
        this.customerDetailsMapper = customerDetailsMapper;
    }

    public Customer toEntity(CreateCustomerDto dto) {
        if (dto == null) {
            return null;
        }

        Customer customer = new Customer();

        if (dto.getEmail() != null) {
            customer.setEmail(dto.getEmail());
        }

        if (dto.getPhoneNumber() != null) {
            customer.setPhoneNumber(dto.getPhoneNumber());
        }

        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());
        customer.setUserName(dto.getUserName());
        customer.setGender(dto.getGender());

        return customer;
    }

    public SaveCustomerDataDto toSaveResponseDto(Customer customer, CustomerDetails customerDetails) {
        SaveCustomerDataDto dto = SaveCustomerDataDto.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .userName(customer.getUserName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .gender(customer.getGender())
                .customerDetails(customerDetailsMapper.toSaveResponseDto(customerDetails))
                .createdBy(customer.getCreatedBy())
                .createdAt(customer.getCreatedAt())
                .updatedBy(customer.getUpdatedBy())
                .updatedAt(customer.getUpdatedAt())
                .build();

        return dto;
    }

}
