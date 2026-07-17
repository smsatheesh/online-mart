package com.onlinemart.customer.controller;

import com.onlinemart.customer.dto.request.customerDetails.ResetPasswordRequestDto;
import com.onlinemart.customer.dto.request.customerDetails.SaveCustomerDetailsDto;
import com.onlinemart.customer.dto.response.ErrorResponseDto;
import com.onlinemart.customer.dto.response.customerDetails.UpdateCustomerDetailsResponseDto;
import com.onlinemart.customer.service.CustomerDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer-details")
@Validated
@Tag(name = "Customer Details", description = "Operation related to customer details")
public class CustomerDetailsController {

    private final CustomerDetailService customerDetailService;

    public CustomerDetailsController(CustomerDetailService customerDetailService) {
        this.customerDetailService = customerDetailService;
    }

    @PatchMapping("/{customerId}/reset-password")
    @Operation(summary = "Reset a customer's password", description = "Hashes and updates the password for the given customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Password reset successfully", content = @Content),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(name = "Validation error", value = """
                                {
                                  "success": false,
                                  "message": "Validation failed: password size must be between 8 and 64",
                                  "errorCode": "VALIDATION_ERROR"
                                }
                                """))),
            @ApiResponse(responseCode = "404", description = "Customer details not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(name = "Customer details not found", value = """
                                {
                                  "success": false,
                                  "message": "Customer details not exists",
                                  "errorCode": "CUSTOMER_DETAILS_NOT_FOUND"
                                }
                                """))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(name = "Internal error", value = """
                                {
                                  "success": false,
                                  "message": "Failed to reset password",
                                  "errorCode": "FAILED_TO_RESET_PASSWORD"
                                }
                                """)))
    })
    public ResponseEntity<Void> resetPassword(
            @Parameter(description = "ID of the customer whose password is being reset", example = "101")
            @PathVariable Long customerId,
            @Valid @RequestBody ResetPasswordRequestDto request) {
        customerDetailService.resetPassword(customerId, request.getPassword());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{customerId}/deactivate")
    @Operation(summary = "Deactivate a customer", description = "Marks the customer's account status as INACTIVE")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Customer deactivated successfully", content = @Content),
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
            @ApiResponse(responseCode = "409", description = "Customer is already inactive",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(name = "Already inactive", value = """
                                {
                                  "success": false,
                                  "message": "Customer is already inactive",
                                  "errorCode": "CUSTOMER_ALREADY_INACTIVE"
                                }
                                """))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(name = "Internal error", value = """
                                {
                                  "success": false,
                                  "message": "Failed to deactivate customer",
                                  "errorCode": "CUSTOMER_DEACTIVATION_FAILED"
                                }
                                """)))
    })
    public ResponseEntity<Void> deactivateCustomer(
            @Parameter(description = "ID of the customer to deactivate", example = "101")
            @PathVariable Long customerId) {
        customerDetailService.deactivateCustomer(customerId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{customerId}")
    @Operation(summary = "Update customer details", description = "Partially updates a customer's profile picture and verification flags")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer details updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UpdateCustomerDetailsResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(name = "Validation error", value = """
                                {
                                  "success": false,
                                  "message": "Validation failed: password must not be null",
                                  "errorCode": "VALIDATION_ERROR"
                                }
                                """))),
            @ApiResponse(responseCode = "404", description = "Customer details not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(name = "Customer details not found", value = """
                                {
                                  "success": false,
                                  "message": "Customer details not exists",
                                  "errorCode": "CUSTOMER_DETAILS_NOT_FOUND"
                                }
                                """))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(name = "Internal error", value = """
                                {
                                  "success": false,
                                  "message": "Customer Details Update Failed",
                                  "errorCode": "CUSTOMER_DETAILS_UPDATE_FAILED"
                                }
                                """)))
    })
    public ResponseEntity<UpdateCustomerDetailsResponseDto> updateCustomerDetails(
            @Parameter(description = "ID of the customer whose details are being updated", example = "101")
            @PathVariable Long customerId,
            @Valid @RequestBody SaveCustomerDetailsDto request) {
        request.setCustomerId(customerId);
        UpdateCustomerDetailsResponseDto response = customerDetailService.updateCustomerDetails(request);
        return ResponseEntity.ok(response);
    }
}
