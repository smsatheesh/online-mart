package com.onlinemart.customer.controller;

import com.onlinemart.customer.dto.request.customer.CreateCustomerDto;
import com.onlinemart.customer.dto.response.customer.CustomerResponseDto;
import com.onlinemart.customer.dto.response.ErrorResponseDto;
import com.onlinemart.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@Validated
@Tag(name = "Customer", description = "Operation related to customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/{customerId}")
    @Operation(summary = "Fetch a customer by ID", description = "Returns customer profile and details for the given customer ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CustomerResponseDto.class),
                            examples = @ExampleObject(name = "Customer found", value = """
                                    {
                                      "id": 101,
                                      "userName": "jane_doe",
                                      "firstName": "Jane",
                                      "lastName": "Doe",
                                      "email": "jane.doe@example.com",
                                      "phoneNumber": "+919876543210",
                                      "gender": "FEMALE",
                                      "updatedAt": "2026-07-16T10:15:30",
                                      "updatedBy": "system"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "Customer or customer details not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(name = "Customer not found", value = """
                                    {
                                      "success": false,
                                      "message": "Customer not exists",
                                      "errorCode": "CUSTOMER_NOT_FOUND"
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(name = "Internal error", value = """
                                    {
                                      "success": false,
                                      "message": "Failed to fetch customer",
                                      "errorCode": "CUSTOMER_FETCH_FAILED"
                                    }
                                    """)))
    })
    public ResponseEntity<CustomerResponseDto> fetchCustomer(
            @Parameter(description = "ID of the customer to fetch", example = "101")
            @PathVariable Long customerId) {
        CustomerResponseDto response = customerService.fetchCustomer(customerId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Onboard a customer",
            description = "Creates a new customer in the e-commerce platform."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Customer onboarded successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomerResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request payload",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Customer already exists",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Customer details required for onboarding",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateCustomerDto.class)
            )
    )
    @PostMapping("")
    public ResponseEntity<CustomerResponseDto> saveCustomer(@Valid @RequestBody CreateCustomerDto request) {
        CustomerResponseDto response = customerService.saveCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}