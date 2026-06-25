package com.onlinemart.cart.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Schema(description = "Request payload to create a cart item")
public class CreateCartItemRequestDto extends BaseCartItemRequestDto {

    @NotNull
    @Schema(description = "Product Identifier", example = "1")
    private Long productId;

}