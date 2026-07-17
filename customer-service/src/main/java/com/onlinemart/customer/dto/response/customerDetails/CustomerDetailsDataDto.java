package com.onlinemart.customer.dto.response.customerDetails;

import com.onlinemart.customer.dto.response.AuditResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "Customer account details.")
public class CustomerDetailsDataDto extends AuditResponseDto {

    @Schema(
            description = "Profile picture URL",
            example = "https://cdn.onlinemart.com/profiles/john-doe.jpg"
    )
    private String profilePic;

    @Schema(
            description = "Whether the customer's email has been verified",
            example = "true"
    )
    private Boolean isEmailVerified;

    @Schema(
            description = "Whether the customer's phone number has been verified",
            example = "false"
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