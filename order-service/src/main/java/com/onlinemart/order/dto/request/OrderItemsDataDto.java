package com.onlinemart.order.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Schema(description = "Request payload to create a order items")
public class OrderItemsDataDto {

    @NotNull
    @Schema(description = "Product Identifier", example = "1")
    private Long productId;

    @NotNull
    @Schema(description = "Quantity of the product", example = "1")
    private Long quantity;

    @NotNull
    @Schema(description = "Unit price of the product", example = "1")
    private Long unitPrice;

}