package com.onlinemart.customer.dto.request.customer;

import com.onlinemart.customer.dto.request.customerDetails.SaveCustomerDetailsDto;
import com.onlinemart.customer.validation.annotation.UserNameOrPhoneRequired;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@UserNameOrPhoneRequired
@Schema(
        name = "CreateCustomerRequest",
        description = "Request payload used to onboard a customer to the e-commerce platform."
)
public class CreateCustomerDto {

    @NotNull
    @Schema(
            description = "Customer's first name",
            example = "John",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String firstName;

    @NotNull
    @Schema(
            description = "Customer's last name",
            example = "Doe",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String lastName;

    @NotNull
    @Schema(
            description = "Unique username for the customer",
            example = "john_doe",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String userName;

    @Schema(
            description = "Customer's email address. Either email or phone number must be provided.",
            example = "john.doe@example.com"
    )
    private String email;

    @Schema(
            description = "Customer's mobile number in international format. Either email or phone number must be provided.",
            example = "+919876543210"
    )
    private String phoneNumber;

    @NotNull
    @Schema(
            description = "Customer gender",
            example = "MALE",
            allowableValues = {"MALE", "FEMALE", "OTHER"},
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String gender;

    @Valid
    @Schema(
            description = "Customer account details",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private SaveCustomerDetailsDto customerDetails;
}