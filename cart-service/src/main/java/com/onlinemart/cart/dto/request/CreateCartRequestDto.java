package com.onlinemart.cart.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Schema(description = "Request payload to create a cart")
public class CreateCartRequestDto implements CartRequestDto {

    @NotNull
    @Schema(description = "Customer identifier", example = "CUST001")
    private Long customerId;

    @NotBlank
    @Schema(description = "Platform where the customer created the cart", example = "MAIN")
    private String platform;

}