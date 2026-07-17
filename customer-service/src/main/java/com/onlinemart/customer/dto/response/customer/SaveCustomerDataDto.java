package com.onlinemart.customer.dto.response.customer;

import com.onlinemart.customer.dto.response.AuditResponseDto;
import com.onlinemart.customer.dto.response.customerDetails.CustomerDetailsDataDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "Customer information.")
public class SaveCustomerDataDto extends AuditResponseDto {

    @Schema(
            description = "Unique identifier of the customer",
            example = "8d5f5482-2ef5-4f7b-8b3d-3df67c52f5e6"
    )
    private Long id;

    @Schema(
            description = "Customer's first name",
            example = "John"
    )
    private String firstName;

    @Schema(
            description = "Customer's last name",
            example = "Doe"
    )
    private String lastName;

    @Schema(
            description = "Customer's username",
            example = "john_doe"
    )
    private String userName;

    @Schema(
            description = "Customer email address",
            example = "john.doe@example.com"
    )
    private String email;

    @Schema(
            description = "Customer phone number",
            example = "+919876543210"
    )
    private String phoneNumber;

    @Schema(
            description = "Customer gender",
            example = "MALE"
    )
    private String gender;

    @Schema(description = "Customer account details")
    private CustomerDetailsDataDto customerDetails;
}