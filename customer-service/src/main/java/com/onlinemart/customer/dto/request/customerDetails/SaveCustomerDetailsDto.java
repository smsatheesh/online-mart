package com.onlinemart.customer.dto.request.customerDetails;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(
        name = "SaveCustomerDetailsRequest",
        description = "Customer account information."
)
public class SaveCustomerDetailsDto {

    @Schema(
            description = "Customer details unique identifier",
            example = "101",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long customerId;

    @Schema(
            description = "Customer account password",
            example = "P@ssw0rd123",
            requiredMode = Schema.RequiredMode.REQUIRED,
            format = "password"
    )
    private String password;

    @Schema(
            description = "Profile picture URL",
            example = "https://cdn.onlinemart.com/profiles/john-doe.jpg",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String profilePic;

    @Schema(
            description = "Indicates whether the customer's email has been verified",
            example = "false",
            defaultValue = "false"
    )
    private Boolean isEmailVerified;

    @Schema(
            description = "Indicates whether the customer's phone number has been verified",
            example = "false",
            defaultValue = "false"
    )
    private Boolean isPhoneVerified;

    @Schema(
            description = "Current account status",
            example = "ACTIVE",
            allowableValues = {
                    "PENDING",
                    "ACTIVE",
                    "SUSPENDED",
                    "INACTIVE"
            }
    )
    private String accountStatus;
}