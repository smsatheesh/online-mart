package com.onlinemart.customer.service;

import com.onlinemart.customer.dto.request.customerDetails.SaveCustomerDetailsDto;
import com.onlinemart.customer.dto.response.customerDetails.CustomerDetailsDataDto;
import com.onlinemart.customer.dto.response.customerDetails.UpdateCustomerDetailsResponseDto;
import com.onlinemart.customer.entity.CustomerDetails;
import com.onlinemart.customer.exception.CustomerServiceException;
import com.onlinemart.customer.mapper.CustomerDetailsMapper;
import com.onlinemart.customer.repository.CustomerDetailRepository;
import com.onlinemart.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerDetailServiceImplTest {

    @Mock private CustomerDetailRepository customerDetailRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private CustomerDetailsMapper customerDetailsMapper;

    @InjectMocks
    private CustomerDetailServiceImpl customerDetailServiceImpl;

    private CustomerDetails customerDetails;

    @BeforeEach
    void setUp() {
        customerDetails = new CustomerDetails();
        customerDetails.setCustomerId(1L);
        customerDetails.setAccountStatus("ACTIVE");
        customerDetails.setPassword("oldHash");
    }

    @Test
    void deactivateCustomer_success_setsInactiveAndSaves() {
        when(customerRepository.existsById(1L)).thenReturn(true);
        when(customerDetailRepository.findByCustomerId(1L)).thenReturn(customerDetails);

        customerDetailServiceImpl.deactivateCustomer(1L);

        assertThat(customerDetails.getAccountStatus()).isEqualTo("INACTIVE");
        verify(customerDetailRepository).save(customerDetails);
    }

    @Test
    void deactivateCustomer_alreadyInactive_throwsConflict() {
        customerDetails.setAccountStatus("INACTIVE");
        when(customerRepository.existsById(1L)).thenReturn(true);
        when(customerDetailRepository.findByCustomerId(1L)).thenReturn(customerDetails);

        CustomerServiceException thrown = catchThrowableOfType(
                () -> customerDetailServiceImpl.deactivateCustomer(1L),
                CustomerServiceException.class
        );

        assertThat(thrown.getErrorResponse().getErrorCode()).isEqualTo("CUSTOMER_ALREADY_INACTIVE");
        verify(customerDetailRepository, never()).save(any());
    }

    @Test
    void deactivateCustomer_customerNotFound_throwsNotFound() {
        when(customerRepository.existsById(99L)).thenReturn(false);

        CustomerServiceException thrown = catchThrowableOfType(
                () -> customerDetailServiceImpl.deactivateCustomer(99L),
                CustomerServiceException.class
        );

        assertThat(thrown.getErrorResponse().getErrorCode()).isEqualTo("CUSTOMER_NOT_FOUND");
    }

    @Test
    void resetPassword_success_hashesAndSaves() {
        when(customerRepository.existsById(1L)).thenReturn(true);
        when(customerDetailRepository.findByCustomerId(1L)).thenReturn(customerDetails);
        when(passwordEncoder.encode("newPlainPassword")).thenReturn("newHashed");

        customerDetailServiceImpl.resetPassword(1L, "newPlainPassword");

        assertThat(customerDetails.getPassword()).isEqualTo("newHashed");
        verify(customerDetailRepository).save(customerDetails);
        verify(passwordEncoder).encode("newPlainPassword");
    }

    @Test
    void resetPassword_customerNotFound_throwsException() {
        when(customerRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> customerDetailServiceImpl.resetPassword(1L, "pw"))
                .isInstanceOf(CustomerServiceException.class);

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void updateCustomerDetails_partialUpdate_onlyChangesProvidedFields() {
        SaveCustomerDetailsDto requestDto = new SaveCustomerDetailsDto();
        requestDto.setCustomerId(1L);
        requestDto.setIsEmailVerified(true);
        // isPhoneVerified and profilePic intentionally left null

        CustomerDetailsDataDto dataDto = CustomerDetailsDataDto.builder()
                .isEmailVerified(true)
                .build();

        when(customerRepository.existsById(1L)).thenReturn(true);
        when(customerDetailRepository.findByCustomerId(1L)).thenReturn(customerDetails);
        when(customerDetailsMapper.toSaveResponseDto(customerDetails)).thenReturn(dataDto);

        UpdateCustomerDetailsResponseDto response = customerDetailServiceImpl.updateCustomerDetails(requestDto);

        assertThat(customerDetails.getIsEmailVerified()).isTrue();
        assertThat(response.getSuccess()).isTrue();
        verify(customerDetailRepository).save(customerDetails);
    }

    @Test
    void fetchCustomerDetailsByCustomerId_notFound_throwsException() {
        when(customerRepository.existsById(1L)).thenReturn(true);
        when(customerDetailRepository.findByCustomerId(1L)).thenReturn(null);

        CustomerServiceException thrown = catchThrowableOfType(
                () -> customerDetailServiceImpl.fetchCustomerDetailsByCustomerId(1L),
                CustomerServiceException.class
        );

        assertThat(thrown.getErrorResponse().getErrorCode()).isEqualTo("CUSTOMER_DETAILS_NOT_FOUND");
    }

    @Test
    void saveCustomerDetails_success_returnsSavedEntity() {
        SaveCustomerDetailsDto requestDto = new SaveCustomerDetailsDto();
        requestDto.setPassword("plain");
        requestDto.setProfilePic("http://cdn/pic.jpg");

        when(customerRepository.existsById(1L)).thenReturn(true);
        when(customerDetailsMapper.toEntity(1L, requestDto)).thenReturn(customerDetails);
        when(customerDetailRepository.saveAndFlush(customerDetails)).thenReturn(customerDetails);

        CustomerDetails result = customerDetailServiceImpl.saveCustomerDetails(1L, requestDto);

        assertThat(result).isEqualTo(customerDetails);
    }
}