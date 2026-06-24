package com.onlinemart.order.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Order Data")
public class OrderItemsDataDto {

    @Schema(description = "Item identifier", example = "1")
    private Long itemId;

    @Schema(description = "Product identifier", example = "1")
    private Long productId;

    @Schema(description = "Quantity of the Product", example = "13")
    private Long quantity;

    @Schema(description = "Price of the product while ordering", example = "100")
    private Long unitPrice;

}