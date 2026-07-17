package com.onlinemart.customer.dto.request.customerAddress;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(
        name = "UpdateCustomerAddressRequest",
        description = "Address details to associate with a customer."
)
public class UpdateAddressRequestDto {

    @Schema(
            description = "Address unique identifier. Do not set on create — assigned by the server.",
            example = "501",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long addressId;

    @Schema(
            description = "Customer this address belongs to",
            example = "101",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long customerId;

    @Schema(
            description = "Type of address",
            example = "HOME",
            allowableValues = {"HOME", "WORK", "OTHER"},
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String addressType;

    @Schema(
            description = "Primary address line",
            example = "221B Baker Street",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String addressFirstLine;

    @Schema(
            description = "Secondary address line (apartment, suite, unit, etc.)",
            example = "Apt 4B"
    )
    private String addressSecondLine;

    @Schema(
            description = "Nearby landmark for easier identification",
            example = "Near Central Park"
    )
    private String landmark;

    @Schema(
            description = "Postal / ZIP code",
            example = "560001",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String postalCode;

    @Schema(
            description = "City",
            example = "Bengaluru",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String city;

    @Schema(
            description = "State / province",
            example = "Karnataka",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String state;

    @Schema(
            description = "Country",
            example = "India",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String country;
}
