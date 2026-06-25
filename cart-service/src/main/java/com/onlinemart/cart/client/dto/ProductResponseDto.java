package com.onlinemart.cart.client.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Standard API response wrapper for product operations")
public class ProductResponseDto {

    @Schema(description = "Indicates operation success", example = "true")
    private boolean success;

    @Schema(description = "Human readable message", example = "Product fetched successfully")
    private String message;

    @Schema(description = "Payload containing product details")
    private ProductDataDto data;

}
