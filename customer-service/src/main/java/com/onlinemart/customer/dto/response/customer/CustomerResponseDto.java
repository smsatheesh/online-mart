package com.onlinemart.customer.dto.response.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
@Schema(description = "Response returned after successfully onboarding a customer.")
public class CustomerResponseDto {

    @Schema(
            description = "Indicates whether the request was successful",
            example = "true"
    )
    private Boolean success;

    @Schema(
            description = "Response message",
            example = "Customer onboarded successfully."
    )
    private String message;

    @Schema(description = "Created customer information")
    private SaveCustomerDataDto data;
}