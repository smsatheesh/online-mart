package com.onlinemart.customer.dto.response.customerAddress;

import com.onlinemart.customer.dto.response.AuditResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Schema(
        name = "CustomerAddressData",
        description = "Stored address details for a customer."
)
public class CustomerAddressDto extends AuditResponseDto {

    @Schema(description = "Address unique identifier", example = "501")
    private Long addressId;

    @Schema(description = "Customer this address belongs to", example = "101")
    private Long customerId;

    @Schema(description = "Type of address", example = "HOME", allowableValues = {"HOME", "WORK", "OTHER"})
    private String addressType;

    @Schema(description = "Primary address line", example = "221B Baker Street")
    private String addressFirstLine;

    @Schema(description = "Secondary address line (apartment, suite, unit, etc.)", example = "Apt 4B")
    private String addressSecondLine;

    @Schema(description = "Nearby landmark for easier identification", example = "Near Central Park")
    private String landmark;

    @Schema(description = "Postal / ZIP code", example = "560001")
    private String postalCode;

    @Schema(description = "City", example = "Bengaluru")
    private String city;

    @Schema(description = "State / province", example = "Karnataka")
    private String state;

    @Schema(description = "Country", example = "India")
    private String country;
}