package com.onlinemart.order.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Schema(description = "Request payload to update a order status")
public class OrderStatusRequestDto {

    private Long orderId;

    @NotNull
    @Schema(description = "Status of the product", example = "SHIPPED")
    private String status;

}