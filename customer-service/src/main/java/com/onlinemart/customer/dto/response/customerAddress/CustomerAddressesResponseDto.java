package com.onlinemart.customer.dto.response.customerAddress;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
@Builder
@Schema(description = "Response returned on the customer address.")
public class CustomerAddressesResponseDto {

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

    @Schema(description = "Map of address type to address details", example = "{\"HOME\": {...}, \"WORK\": {...}}")
    private Map<String, CustomerAddressDto> data;
}
