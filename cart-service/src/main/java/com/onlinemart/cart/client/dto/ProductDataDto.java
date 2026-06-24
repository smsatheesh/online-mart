package com.onlinemart.cart.client.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "Product payload returned by product service")
public class ProductDataDto {

    @Schema(description = "Product id exposed to clients", example = "PROD001")
    private String productId;

    @Schema(description = "Price in the smallest currency unit", example = "55000")
    private Long price;

    @Schema(description = "Available stock quantity", example = "15")
    private Long availableQuantity;

}
