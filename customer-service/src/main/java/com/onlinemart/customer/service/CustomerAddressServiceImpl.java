package com.onlinemart.customer.service;

import com.onlinemart.customer.dto.request.customerAddress.CreateCustomerAddressRequestDto;
import com.onlinemart.customer.dto.request.customerAddress.UpdateAddressRequestDto;
import com.onlinemart.customer.dto.response.ErrorResponseDto;
import com.onlinemart.customer.dto.response.customerAddress.CustomerAddressDto;
import com.onlinemart.customer.dto.response.customerAddress.CustomerAddressResponseDto;
import com.onlinemart.customer.dto.response.customerAddress.CustomerAddressesResponseDto;
import com.onlinemart.customer.entity.CustomerAddress;
import com.onlinemart.customer.exception.CustomerConstraintViolation;
import com.onlinemart.customer.exception.CustomerServiceException;
import com.onlinemart.customer.mapper.CustomerAddressMapper;
import com.onlinemart.customer.repository.CustomerAddressRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CustomerAddressServiceImpl implements CustomerAddressService {

    private final CustomerAddressRepository customerAddressRepository;
    private final CustomerAddressMapper customerAddressMapper;
    private final CustomerServiceImpl customerServiceImpl;

    public CustomerAddressServiceImpl(CustomerAddressRepository customerAddressRepository, CustomerAddressMapper customerAddressMapper, CustomerServiceImpl customerServiceImpl) {
        this.customerAddressRepository = customerAddressRepository;
        this.customerAddressMapper = customerAddressMapper;
        this.customerServiceImpl = customerServiceImpl;
    }

    @Override
    public CustomerAddressResponseDto createAddress(CreateCustomerAddressRequestDto requestDto) {
        try {
            customerServiceImpl.fetchCustomerById(requestDto.getCustomerId());
            CustomerAddress customerAddress = customerAddressMapper.toEntity(requestDto.getCustomerId(), requestDto);
            customerAddressRepository.save(customerAddress);
            CustomerAddressDto dataDto = customerAddressMapper.toSaveResponseDto(customerAddress);

            return CustomerAddressResponseDto.builder()
                    .success(Boolean.TRUE)
                    .message("Customer address created successfully")
                    .data(dataDto)
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
            log.error("Unexpected error at createAddress: {}", ex.getMessage(), ex);
            ErrorResponseDto error = ErrorResponseDto.builder()
                    .success(Boolean.FALSE)
                    .message("Failed to create customer address")
                    .errorCode("CUSTOMER_ADDRESS_CREATE_FAILED")
                    .build();
            throw new CustomerServiceException(error);
        }
    }

    @Override
    public CustomerAddressResponseDto updateAddress(UpdateAddressRequestDto requestDto) {
        try {
            customerServiceImpl.fetchCustomerById(requestDto.getCustomerId());

            CustomerAddress existingDetails = customerAddressRepository.findById(requestDto.getAddressId())
                    .orElseThrow(
                            () -> buildException("Customer Address not found", "CUSTOMER_ADDRESS_NOT_FOUND")
                    );

            if (requestDto.getAddressType() != null) {
                existingDetails.setAddressType(requestDto.getAddressType());
            }

            if (requestDto.getAddressFirstLine() != null) {
                existingDetails.setAddressFirstLine(requestDto.getAddressFirstLine());
            }

            if (requestDto.getAddressSecondLine() != null) {
                existingDetails.setAddressSecondLine(requestDto.getAddressSecondLine());
            }

            if (requestDto.getLandmark() != null) {
                existingDetails.setLandmark(requestDto.getLandmark());
            }

            if (requestDto.getPostalCode() != null) {
                existingDetails.setPostalCode(requestDto.getPostalCode());
            }

            if (requestDto.getCity() != null) {
                existingDetails.setCity(requestDto.getCity());
            }

            if (requestDto.getState() != null) {
                existingDetails.setState(requestDto.getState());
            }

            if (requestDto.getCountry() != null) {
                existingDetails.setCountry(requestDto.getCountry());
            }

            customerAddressRepository.save(existingDetails);

            return CustomerAddressResponseDto.builder()
                    .success(Boolean.TRUE)
                    .message("Customer address updated successfully")
                    .data(customerAddressMapper.toSaveResponseDto(existingDetails))
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
            log.error("Unexpected error at updateAddress: {}", ex.getMessage(), ex);
            ErrorResponseDto error = ErrorResponseDto.builder()
                    .success(Boolean.FALSE)
                    .message("Failed to update customer address")
                    .errorCode("CUSTOMER_ADDRESS_UPDATE_FAILED")
                    .build();
            throw new CustomerServiceException(error);
        }
    }

    @Override
    public CustomerAddressesResponseDto fetchAddresses(Long customerId) {
        try {
            List<CustomerAddress> addresses = customerAddressRepository.findByCustomerId(customerId);

            Map<String, CustomerAddressDto> addressMap = addresses.stream()
                    .collect(Collectors.toMap(
                            CustomerAddress::getAddressType,
                            customerAddressMapper::toSaveResponseDto
                    ));

            return CustomerAddressesResponseDto.builder()
                    .success(Boolean.TRUE)
                    .message("Customer addresses fetched successfully")
                    .data(addressMap)
                    .build();

        } catch (CustomerServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error in fetchAddresses for customerId {}: {}", customerId, ex.getMessage(), ex);
            ErrorResponseDto error = ErrorResponseDto.builder()
                    .success(Boolean.FALSE)
                    .message("Failed to fetch customer addresses")
                    .errorCode("CUSTOMER_ADDRESS_FETCH_FAILED")
                    .build();
            throw new CustomerServiceException(error);
        }
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
