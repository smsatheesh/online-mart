package com.onlinemart.customer.service;

import com.onlinemart.customer.dto.request.customerAddress.CreateCustomerAddressRequestDto;
import com.onlinemart.customer.dto.request.customerAddress.UpdateAddressRequestDto;
import com.onlinemart.customer.dto.response.customerAddress.CustomerAddressDto;
import com.onlinemart.customer.dto.response.customerAddress.CustomerAddressResponseDto;
import com.onlinemart.customer.dto.response.customerAddress.CustomerAddressesResponseDto;
import com.onlinemart.customer.entity.Customer;
import com.onlinemart.customer.entity.CustomerAddress;
import com.onlinemart.customer.exception.CustomerServiceException;
import com.onlinemart.customer.mapper.CustomerAddressMapper;
import com.onlinemart.customer.repository.CustomerAddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerAddressServiceImplTest {

    @Mock private CustomerAddressRepository customerAddressRepository;
    @Mock private CustomerAddressMapper customerAddressMapper;
    @Mock private CustomerServiceImpl customerServiceImpl;

    @InjectMocks
    private CustomerAddressServiceImpl customerAddressServiceImpl;

    private CustomerAddress customerAddress;

    @BeforeEach
    void setUp() {
        customerAddress = new CustomerAddress();
        customerAddress.setAddressId(1L);
        customerAddress.setCustomerId(1L);
        customerAddress.setAddressType("HOME");
        customerAddress.setCity("Bengaluru");
    }

    @Test
    void createAddress_success_returnsResponse() {
        CreateCustomerAddressRequestDto requestDto = new CreateCustomerAddressRequestDto();
        requestDto.setCustomerId(1L);
        requestDto.setAddressType("HOME");

        CustomerAddressDto dataDto = CustomerAddressDto.builder().addressId(1L).build();

        when(customerServiceImpl.fetchCustomerById(1L)).thenReturn(new Customer());
        when(customerAddressMapper.toEntity(1L, requestDto)).thenReturn(customerAddress);
        when(customerAddressMapper.toSaveResponseDto(customerAddress)).thenReturn(dataDto);

        CustomerAddressResponseDto response = customerAddressServiceImpl.createAddress(requestDto);

        assertThat(response.getSuccess()).isTrue();
        verify(customerAddressRepository).save(customerAddress);
    }

    @Test
    void createAddress_customerNotFound_propagatesException() {
        CreateCustomerAddressRequestDto requestDto = new CreateCustomerAddressRequestDto();
        requestDto.setCustomerId(99L);

        when(customerServiceImpl.fetchCustomerById(99L))
                .thenThrow(new CustomerServiceException(null));

        assertThatThrownBy(() -> customerAddressServiceImpl.createAddress(requestDto))
                .isInstanceOf(CustomerServiceException.class);

        verify(customerAddressRepository, never()).save(any());
    }

    @Test
    void updateAddress_success_updatesOnlyProvidedFields() {
        UpdateAddressRequestDto requestDto = new UpdateAddressRequestDto();
        requestDto.setCustomerId(1L);
        requestDto.setAddressId(1L);
        requestDto.setCity("Chennai");

        CustomerAddressDto dataDto = CustomerAddressDto.builder().city("Chennai").build();

        when(customerServiceImpl.fetchCustomerById(1L)).thenReturn(new Customer());
        when(customerAddressRepository.findById(1L)).thenReturn(Optional.of(customerAddress));
        when(customerAddressMapper.toSaveResponseDto(customerAddress)).thenReturn(dataDto);

        CustomerAddressResponseDto response = customerAddressServiceImpl.updateAddress(requestDto);

        assertThat(customerAddress.getCity()).isEqualTo("Chennai");
        assertThat(response.getSuccess()).isTrue();
        verify(customerAddressRepository).save(customerAddress);
    }

    @Test
    void updateAddress_addressNotFound_throwsException() {
        UpdateAddressRequestDto requestDto = new UpdateAddressRequestDto();
        requestDto.setCustomerId(1L);
        requestDto.setAddressId(99L);

        when(customerServiceImpl.fetchCustomerById(1L)).thenReturn(new Customer());
        when(customerAddressRepository.findById(99L)).thenReturn(Optional.empty());

        CustomerServiceException thrown = catchThrowableOfType(
                () -> customerAddressServiceImpl.updateAddress(requestDto),
                CustomerServiceException.class
        );

        assertThat(thrown.getErrorResponse().getErrorCode()).isEqualTo("CUSTOMER_ADDRESS_NOT_FOUND");
    }

    @Test
    void fetchAddresses_success_returnsAll() {
        CustomerAddressDto dataDto = CustomerAddressDto.builder().addressId(1L).addressType("HOME").build();

        when(customerAddressRepository.findByCustomerId(1L)).thenReturn(List.of(customerAddress));
        when(customerAddressMapper.toSaveResponseDto(customerAddress)).thenReturn(dataDto);

        CustomerAddressesResponseDto response = customerAddressServiceImpl.fetchAddresses(1L);

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData()).isNotEmpty();
    }

    @Test
    void fetchAddresses_noAddresses_returnsEmptySuccessResponse() {
        when(customerAddressRepository.findByCustomerId(1L)).thenReturn(Collections.emptyList());

        CustomerAddressesResponseDto response = customerAddressServiceImpl.fetchAddresses(1L);

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData()).isEmpty();
    }
}