package com.onlinemart.customer.service;

import com.onlinemart.customer.dto.request.customer.CreateCustomerDto;
import com.onlinemart.customer.dto.response.customer.CustomerResponseDto;
import com.onlinemart.customer.dto.response.ErrorResponseDto;
import com.onlinemart.customer.dto.response.customer.SaveCustomerDataDto;
import com.onlinemart.customer.entity.Customer;
import com.onlinemart.customer.entity.CustomerDetails;
import com.onlinemart.customer.exception.CustomerConstraintViolation;
import com.onlinemart.customer.exception.CustomerServiceException;
import com.onlinemart.customer.mapper.CustomerMapper;
import com.onlinemart.customer.repository.CustomerDetailRepository;
import com.onlinemart.customer.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final CustomerDetailServiceImpl customerDetailServiceImp;

    public CustomerServiceImpl(
            CustomerRepository customerRepository,
            CustomerDetailRepository customerDetailRepository,
            CustomerMapper customerMapper,
            CustomerDetailServiceImpl customerDetailServiceImp
    ) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
        this.customerDetailServiceImp = customerDetailServiceImp;
    }

    @Override
    @Transactional
    public CustomerResponseDto saveCustomer(CreateCustomerDto createCustomerDto) {
        try {
            Customer customer = customerMapper.toEntity(createCustomerDto);
            customerRepository.save(customer);

            CustomerDetails customerDetails = customerDetailServiceImp.saveCustomerDetails(customer.getId(), createCustomerDto.getCustomerDetails());
            SaveCustomerDataDto responseData = customerMapper.toSaveResponseDto(customer, customerDetails);

            return CustomerResponseDto.builder()
                    .success(Boolean.TRUE)
                    .message("Customer on-boarded successfully")
                    .data(responseData)
                    .build();
        } catch (CustomerServiceException ex) {
            throw ex;
        } catch (DataIntegrityViolationException ex) {
            String constraintName = extractConstraintName(ex);
            CustomerConstraintViolation violation = CustomerConstraintViolation.fromConstraintName(constraintName)
                    .orElse(null);

            if (violation == null) {
                log.error("Unmapped constraint violation [{}]: {}", constraintName, ex.getMessage(), ex);
            } else {
                log.warn("Constraint violation [{}]: {}", constraintName, violation.getMessage());
            }

            ErrorResponseDto error = ErrorResponseDto.builder()
                    .success(Boolean.FALSE)
                    .message(violation != null ? violation.getMessage() : "Customer data conflicts with an existing record")
                    .errorCode(violation != null ? violation.getErrorCode() : "CUSTOMER_DATA_CONFLICT")
                    .build();
            throw new CustomerServiceException(error);
        } catch (Exception ex) {
            log.error("Unexpected error in saveCustomer: {}", ex.getMessage(), ex);
            ErrorResponseDto error = ErrorResponseDto.builder()
                    .success(Boolean.FALSE)
                    .message("Failed to create customer")
                    .errorCode("CUSTOMER_CREATE_FAILED")
                    .build();
            throw new CustomerServiceException(error);
        }
    }

    @Override
    public CustomerResponseDto fetchCustomer(Long customerId) {
        try {
            Customer customer = fetchCustomerById(customerId);
            CustomerDetails customerDetails = customerDetailServiceImp.fetchCustomerDetailsByCustomerId(customerId);

            SaveCustomerDataDto response = customerMapper.toSaveResponseDto(customer, customerDetails);

            return CustomerResponseDto.builder()
                    .success(Boolean.TRUE)
                    .message("Customer details fetched successfully")
                    .data(response)
                    .build();

        } catch (CustomerServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error in fetchCustomer: {}", ex.getMessage(), ex);
            ErrorResponseDto error = ErrorResponseDto.builder()
                    .success(Boolean.FALSE)
                    .message("Failed to fetch customer")
                    .errorCode("CUSTOMER_FETCH_FAILED")
                    .build();
            throw new CustomerServiceException(error);
        }
    }

    public Customer fetchCustomerById(Long customerId) {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(
                            () -> buildException("Customer not exists", "CUSTOMER_NOT_FOUND")
                    );
            return customer;
    }

    private String extractConstraintName(DataIntegrityViolationException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof org.hibernate.exception.ConstraintViolationException cve) {
            return cve.getConstraintName();
        }
        return null;
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