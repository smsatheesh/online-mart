package com.onlinemart.order.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Standard API response wrapper for order operation")
public class OrderDetailResponseDto {

    @Schema(description = "Indicates operation success", example = "true")
    private Boolean success;

    @Schema(description = "Human readable message", example = "Order created successfully")
    private String message;

    @Schema(description = "Payload containing order details")
    private OrderDto data;

}
