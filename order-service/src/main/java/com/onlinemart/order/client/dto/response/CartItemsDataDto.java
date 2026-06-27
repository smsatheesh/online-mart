package com.onlinemart.order.client.dto.response;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Cart Items payload returned in the response")
public class CartItemsDataDto implements Serializable {

    @Schema(description = "Item identifier", example = "1")
    private Long itemId;

    @Schema(description = "Product identifier", example = "1")
    private Long productId;

    @Schema(description = "Quantity of the product in the cart", example = "13")
    private Long quantity;

    @Schema(description = "Unit price of the product in the cart", example = "100")
    private Long unitPrice;

}
