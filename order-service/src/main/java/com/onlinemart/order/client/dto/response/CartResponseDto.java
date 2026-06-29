package com.onlinemart.order.client.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "API response wrapper for cart operations")
public class CartResponseDto {

    @Schema(description = "Indicates operation success", example = "true")
    private Boolean success;

    @Schema(description = "Human readable message", example = "Cart saved successfully")
    private String message;

    @Schema(description = "Payload containing cart and item details")
    private CartDataDto data;
}