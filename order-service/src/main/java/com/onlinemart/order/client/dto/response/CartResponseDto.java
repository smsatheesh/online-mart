package com.onlinemart.order.client.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Standard API response wrapper for cart operation")
public class CartResponseDto implements Serializable {

    @Schema(description = "Indicates operation success", example = "true")
    private Boolean success;

    @Schema(description = "Human readable message", example = "Cart created successfully")
    private String message;

    @Schema(description = "Payload containing cart details")
    private CartDataDto data;

}
