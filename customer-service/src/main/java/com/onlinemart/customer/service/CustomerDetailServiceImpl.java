package com.onlinemart.customer.service;

import com.onlinemart.customer.dto.request.customerDetails.SaveCustomerDetailsDto;
import com.onlinemart.customer.dto.response.customerDetails.CustomerDetailsDataDto;
import com.onlinemart.customer.dto.response.ErrorResponseDto;
import com.onlinemart.customer.dto.response.customerDetails.UpdateCustomerDetailsResponseDto;
import com.onlinemart.customer.entity.CustomerDetails;
import com.onlinemart.customer.exception.CustomerServiceException;
import com.onlinemart.customer.mapper.CustomerDetailsMapper;
import com.onlinemart.customer.repository.CustomerDetailRepository;
import com.onlinemart.customer.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomerDetailServiceImpl implements CustomerDetailService {

    private final CustomerDetailRepository customerDetailRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerDetailsMapper customerDetailsMapper;

    public CustomerDetailServiceImpl(
            CustomerDetailRepository customerDetailRepository,
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder,
            CustomerDetailsMapper customerDetailsMapper
    ) {
        this.customerDetailRepository = customerDetailRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.customerDetailsMapper = customerDetailsMapper;
    }

    @Override
    public void deactivateCustomer(Long customerId) {
        try {
            CustomerDetails customerDetails = fetchCustomerDetailsByCustomerId(customerId);
            if ("INACTIVE".equalsIgnoreCase(customerDetails.getAccountStatus())) {
                throw buildException("Customer is already inactive", "CUSTOMER_ALREADY_INACTIVE");
            }

            customerDetails.setAccountStatus("INACTIVE");
            customerDetailRepository.save(customerDetails);
        } catch (CustomerServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error in deactivateCustomer for customerId {}: {}", customerId, ex.getMessage(), ex);
            ErrorResponseDto error = ErrorResponseDto.builder()
                    .success(Boolean.FALSE)
                    .message("Failed to deactivate customer")
                    .errorCode("CUSTOMER_DEACTIVATION_FAILED")
                    .build();
            throw new CustomerServiceException(error);
        }
    }

    @Override
    public void resetPassword(Long customerId, String password) {
        try {
            validateCustomerExists(customerId);
            CustomerDetails customerDetails = fetchCustomerDetailsByCustomerId(customerId);
            String hashedPassword = passwordEncoder.encode(password);
            customerDetails.setPassword(hashedPassword);
            customerDetailRepository.save(customerDetails);
        } catch (CustomerServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error in resetPassword for customerId {}: {}", customerId, ex.getMessage(), ex);
            ErrorResponseDto error = ErrorResponseDto.builder()
                    .success(Boolean.FALSE)
                    .message("Failed to reset password")
                    .errorCode("FAILED_TO_RESET_PASSWORD")
                    .build();
            throw new CustomerServiceException(error);
        }
    }

    @Override
    @Transactional
    public UpdateCustomerDetailsResponseDto updateCustomerDetails(SaveCustomerDetailsDto requestDto) {
        try {
            validateCustomerExists(requestDto.getCustomerId());
            CustomerDetails existingCustomerDetails = fetchCustomerDetailsByCustomerId(requestDto.getCustomerId());

            if (requestDto.getIsEmailVerified() != null) {
                existingCustomerDetails.setIsEmailVerified(requestDto.getIsEmailVerified());
            }
            if (requestDto.getIsPhoneVerified() != null) {
                existingCustomerDetails.setIsPhoneVerified(requestDto.getIsPhoneVerified());
            }
            if (requestDto.getProfilePic() != null) {
                existingCustomerDetails.setProfilePic(requestDto.getProfilePic());
            }

            customerDetailRepository.save(existingCustomerDetails);

            CustomerDetailsDataDto dataDto = customerDetailsMapper.toSaveResponseDto(existingCustomerDetails);

            return UpdateCustomerDetailsResponseDto.builder()
                    .success(Boolean.TRUE)
                    .message("Customer details updated successfully")
                    .data(dataDto)
                    .build();

        } catch (CustomerServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error in updateCustomerDetails for customerId {}: {}",
                    requestDto.getCustomerId(), ex.getMessage(), ex);
            ErrorResponseDto error = ErrorResponseDto.builder()
                    .success(Boolean.FALSE)
                    .message("Customer Details Update Failed")
                    .errorCode("CUSTOMER_DETAILS_UPDATE_FAILED")
                    .build();
            throw new CustomerServiceException(error);
        }
    }

    @Transactional
    public CustomerDetails saveCustomerDetails(Long customerId, SaveCustomerDetailsDto requestDetails) {
        try {
            validateCustomerExists(customerId);
            CustomerDetails customerDetails = customerDetailsMapper.toEntity(customerId, requestDetails);
            customerDetailRepository.saveAndFlush(customerDetails);

            return customerDetails;
        } catch (CustomerServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error in saveCustomerDetails for customerId {}: {}", customerId, ex.getMessage(), ex);
            ErrorResponseDto error = ErrorResponseDto.builder()
                    .success(Boolean.FALSE)
                    .message("Failed to create customer")
                    .errorCode("CUSTOMER_CREATE_FAILED")
                    .build();
            throw new CustomerServiceException(error);
        }
    }

    public CustomerDetails fetchCustomerDetailsByCustomerId(Long customerId) {
        try {
            validateCustomerExists(customerId);
            CustomerDetails customerDetails = customerDetailRepository.findByCustomerId(customerId);
            if (customerDetails == null) {
                throw buildException("Customer details not exists", "CUSTOMER_DETAILS_NOT_FOUND");
            }
            return customerDetails;
        } catch (CustomerServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error fetching customer details for customerId {}: {}", customerId, ex.getMessage(), ex);
            ErrorResponseDto error = ErrorResponseDto.builder()
                    .success(Boolean.FALSE)
                    .message("Customer details fetch failed")
                    .errorCode("CUSTOMER_DETAILS_FETCH_ERROR")
                    .build();
            throw new CustomerServiceException(error);
        }
    }

    private void validateCustomerExists(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw buildException("Customer not exists", "CUSTOMER_NOT_FOUND");
        }
    }

    private CustomerServiceException buildException(String message, String errorCode) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .success(Boolean.FALSE)
                .message(message)
                .errorCode(errorCode)
                .build();
        return new CustomerServiceException(error);
    }
}