package com.onlinemart.customer.controller;

import com.onlinemart.customer.dto.request.customerAddress.CreateCustomerAddressRequestDto;
import com.onlinemart.customer.dto.request.customerAddress.UpdateAddressRequestDto;
import com.onlinemart.customer.dto.response.ErrorResponseDto;
import com.onlinemart.customer.dto.response.customerAddress.CustomerAddressResponseDto;
import com.onlinemart.customer.dto.response.customerAddress.CustomerAddressesResponseDto;
import com.onlinemart.customer.service.CustomerAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer-addresses")
@RequiredArgsConstructor
@Tag(name = "Customer Address", description = "Customer address management APIs")
public class CustomerAddressController {

    private final CustomerAddressService customerAddressService;

    @PostMapping("")
    @Operation(summary = "Create a customer address", description = "Adds a new address for the given customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer address created successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CustomerAddressResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(name = "Validation error", value = """
                                    {
                                      "success": false,
                                      "message": "Validation failed: addressFirstLine must not be null",
                                      "errorCode": "VALIDATION_ERROR"
                                    }
                                    """))),
            @ApiResponse(responseCode = "409", description = "Address conflicts with an existing record",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(name = "Conflict", value = """
                                    {
                                      "success": false,
                                      "message": "Customer data conflicts with an existing record",
                                      "errorCode": "CUSTOMER_DATA_CONFLICT"
                                    }
                                    """))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(name = "Internal error", value = """
                                    {
                                      "success": false,
                                      "message": "Failed to create customer address",
                                      "errorCode": "CUSTOMER_ADDRESS_CREATE_FAILED"
                                    }
                                    """)))
    })
    public ResponseEntity<CustomerAddressResponseDto> createAddress(
            @Parameter(description = "ID of the customer this address belongs to", example = "101")
            @Valid @RequestBody CreateCustomerAddressRequestDto request) {
        CustomerAddressResponseDto response = customerAddressService.createAddress(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{addressId}")
    @Operation(summary = "Update a customer address", description = "Partially updates an existing address belonging to the given customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer address updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CustomerAddressResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Customer address not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(name = "Address not found", value = """
                                    {
                                      "success": false,
                                      "message": "Customer Address not found",
                                      "errorCode": "CUSTOMER_ADDRESS_NOT_FOUND"
                                    }
                                    """))),
            @ApiResponse(responseCode = "409", description = "Address conflicts with an existing record",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(name = "Internal error", value = """
                                    {
                                      "success": false,
                                      "message": "Failed to update customer address",
                                      "errorCode": "CUSTOMER_ADDRESS_UPDATE_FAILED"
                                    }
                                    """)))
    })
    public ResponseEntity<CustomerAddressResponseDto> updateAddress(
            @Parameter(description = "ID of the customer who owns this address", example = "101")
            @PathVariable Long addressId,
            @Valid @RequestBody UpdateAddressRequestDto request) {
        request.setAddressId(addressId);
        CustomerAddressResponseDto response = customerAddressService.updateAddress(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{customerId}")
    @Operation(summary = "Fetch all addresses for a customer", description = "Returns every saved address belonging to the given customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Addresses fetched successfully (empty array if none exist)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CustomerAddressesResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(name = "Internal error", value = """
                                {
                                  "success": false,
                                  "message": "Failed to fetch customer addresses",
                                  "errorCode": "CUSTOMER_ADDRESS_FETCH_FAILED"
                                }
                                """)))
    })
    public ResponseEntity<CustomerAddressesResponseDto> fetchAddresses(
            @Parameter(description = "ID of the customer whose addresses are being fetched", example = "101")
            @PathVariable Long customerId) {
        CustomerAddressesResponseDto response = customerAddressService.fetchAddresses(customerId);
        return ResponseEntity.ok(response);
    }

}