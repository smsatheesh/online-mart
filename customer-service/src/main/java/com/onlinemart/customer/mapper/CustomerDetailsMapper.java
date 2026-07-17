package com.onlinemart.customer.mapper;

import com.onlinemart.customer.dto.request.customerDetails.SaveCustomerDetailsDto;
import com.onlinemart.customer.dto.response.customerDetails.CustomerDetailsDataDto;
import com.onlinemart.customer.entity.CustomerDetails;
import org.springframework.stereotype.Component;

@Component
public class CustomerDetailsMapper {

    public CustomerDetails toEntity(Long customerId, SaveCustomerDetailsDto dto) {
        if (dto == null ){
            return null;
        }

        CustomerDetails customerDetails = new CustomerDetails();
        customerDetails.setCustomerId(customerId);
        customerDetails.setPassword(dto.getPassword());
        customerDetails.setProfilePic(dto.getProfilePic());
        customerDetails.setIsEmailVerified(Boolean.FALSE);
        customerDetails.setIsPhoneVerified(Boolean.FALSE);
        customerDetails.setAccountStatus("ACTIVE");

        return customerDetails;
    }

    public CustomerDetailsDataDto toSaveResponseDto(CustomerDetails customerDetails) {
        CustomerDetailsDataDto dto = CustomerDetailsDataDto.builder()
                .profilePic(customerDetails.getProfilePic())
                .isEmailVerified(customerDetails.getIsEmailVerified())
                .isPhoneVerified(customerDetails.getIsPhoneVerified())
                .accountStatus(customerDetails.getAccountStatus())
                .createdBy(customerDetails.getCreatedBy())
                .createdAt(customerDetails.getCreatedAt())
                .updatedBy(customerDetails.getUpdatedBy())
                .updatedAt(customerDetails.getUpdatedAt())
                .build();

        return dto;
    }

}
