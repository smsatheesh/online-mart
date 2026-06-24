package com.onlinemart.cart.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Schema(description = "Request payload to udpate a cart items")
public class UpdateCartItemRequestDto extends BaseCartItemRequestDto {

    @NotBlank
    @Schema(description = "Quantity of the product", example = "12")
    private Long quantity;

    @Schema(description = "Cart item identifier", example = "12")
    private Long itemId;

}