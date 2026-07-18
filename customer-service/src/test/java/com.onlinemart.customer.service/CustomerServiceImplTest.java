package com.onlinemart.customer.service;

import com.onlinemart.customer.dto.request.customer.CreateCustomerDto;
import com.onlinemart.customer.dto.response.customer.CustomerResponseDto;
import com.onlinemart.customer.dto.response.customer.SaveCustomerDataDto;
import com.onlinemart.customer.entity.Customer;
import com.onlinemart.customer.entity.CustomerDetails;
import com.onlinemart.customer.exception.CustomerServiceException;
import com.onlinemart.customer.mapper.CustomerMapper;
import com.onlinemart.customer.outbox.OutboxWriter;
import com.onlinemart.customer.repository.CustomerDetailRepository;
import com.onlinemart.customer.repository.CustomerRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerDetailRepository customerDetailRepository; // unused by impl directly but kept for constructor parity

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private OutboxWriter outboxWriter;

    @Mock
    private CustomerDetailServiceImpl customerDetailServiceImpl;

    @InjectMocks
    private CustomerServiceImpl customerServiceImpl;

    private Customer customer;
    private CreateCustomerDto createCustomerDto;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("Jane");
        customer.setLastName("Doe");
        customer.setUserName("jane_doe");
        customer.setEmail("jane@example.com");
        customer.setPhoneNumber("+919876543210");
        customer.setGender("FEMALE");

        createCustomerDto = new CreateCustomerDto();
        // populate fields as per your actual DTO shape
    }

    @Test
    void saveCustomer_success_returnsResponseWithData() {
        CustomerDetails customerDetails = new CustomerDetails();
        SaveCustomerDataDto responseData = SaveCustomerDataDto.builder()
                .id(1L)
                .firstName("Jane")
                .build();

        when(customerMapper.toEntity(createCustomerDto)).thenReturn(customer);
        when(customerRepository.save(customer)).thenReturn(customer);   // <-- ADD THIS LINE
        when(customerDetailServiceImpl.saveCustomerDetails(eq(1L), any())).thenReturn(customerDetails);
        when(customerMapper.toSaveResponseDto(customer, customerDetails)).thenReturn(responseData);

        CustomerResponseDto response = customerServiceImpl.saveCustomer(createCustomerDto);

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Customer on-boarded successfully");
        assertThat(response.getData()).isEqualTo(responseData);
        verify(customerRepository).save(customer);
    }
    @Test
    void saveCustomer_duplicateUsername_throwsConflictException() {
        when(customerMapper.toEntity(createCustomerDto)).thenReturn(customer);

        ConstraintViolationException hibernateEx = new ConstraintViolationException(
                "duplicate key", new SQLException(), "unq_customers_user_name");
        DataIntegrityViolationException dive = new DataIntegrityViolationException("insert failed", hibernateEx);

        doThrow(dive).when(customerRepository).save(customer);

        CustomerServiceException thrown = catchThrowableOfType(
                () -> customerServiceImpl.saveCustomer(createCustomerDto),
                CustomerServiceException.class
        );

        assertThat(thrown).isNotNull();
        assertThat(thrown.getErrorResponse().getErrorCode()).isEqualTo("USERNAME_ALREADY_EXISTS");
    }

    @Test
    void saveCustomer_unexpectedError_wrapsAsCreateFailed() {
        when(customerMapper.toEntity(createCustomerDto)).thenReturn(customer);
        doThrow(new RuntimeException("db down")).when(customerRepository).save(customer);

        CustomerServiceException thrown = catchThrowableOfType(
                () -> customerServiceImpl.saveCustomer(createCustomerDto),
                CustomerServiceException.class
        );

        assertThat(thrown.getErrorResponse().getErrorCode()).isEqualTo("CUSTOMER_CREATE_FAILED");
    }

    @Test
    void fetchCustomer_success_returnsData() {
        CustomerDetails customerDetails = new CustomerDetails();
        SaveCustomerDataDto responseData = SaveCustomerDataDto.builder().id(1L).build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerDetailServiceImpl.fetchCustomerDetailsByCustomerId(1L)).thenReturn(customerDetails);
        when(customerMapper.toSaveResponseDto(customer, customerDetails)).thenReturn(responseData);

        CustomerResponseDto response = customerServiceImpl.fetchCustomer(1L);

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo(responseData);
    }

    @Test
    void fetchCustomer_customerNotFound_throwsCustomerServiceException() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        CustomerServiceException thrown = catchThrowableOfType(
                () -> customerServiceImpl.fetchCustomer(99L),
                CustomerServiceException.class
        );

        assertThat(thrown.getErrorResponse().getErrorCode()).isEqualTo("CUSTOMER_NOT_FOUND");
    }

    @Test
    void fetchCustomerById_found_returnsCustomer() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        Customer result = customerServiceImpl.fetchCustomerById(1L);

        assertThat(result).isEqualTo(customer);
    }

    @Test
    void fetchCustomerById_notFound_throwsException() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerServiceImpl.fetchCustomerById(1L))
                .isInstanceOf(CustomerServiceException.class);
    }
}