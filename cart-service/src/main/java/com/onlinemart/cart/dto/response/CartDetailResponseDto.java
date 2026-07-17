package com.onlinemart.cart.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "API response wrapper for cart detail (no items)")
public class CartDetailResponseDto {

    @Schema(description = "Indicates operation success", example = "true")
    private Boolean success;

    @Schema(description = "Human readable message", example = "Cart fetched successfully")
    private String message;

    @Schema(description = "Payload containing cart details without items")
    private List<CartDto> data;

}
