package com.onlinemart.cart.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Schema(description = "Request payload to create a cart item")
public class BaseCartItemRequestDto {

    @NotNull
    @Schema(description = "Cart identifier", example = "1")
    private Long cartId;

    @NotNull
    @Positive
    @Schema(description = "Quantity of the product", example = "2")
    private Long quantity;

}