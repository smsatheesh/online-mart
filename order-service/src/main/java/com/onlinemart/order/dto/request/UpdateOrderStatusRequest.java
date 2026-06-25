package com.onlinemart.order.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Schema(description = "Request payload to update an order status")
public class UpdateOrderStatusRequest {

    @NotNull
    @Schema(description = "Status of the order", example = "PLACED")
    private String status;

}