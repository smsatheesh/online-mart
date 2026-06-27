package com.onlinemart.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Schema(description = "Request payload to create a order")
public class CreateOrderRequestDto {

    @NotNull
    @Schema(description = "Cart Identifier", example = "1")
    private Long cartId;

    @NotNull
    @Schema(description = "Cart Identifier", example = "1")
    private Long customerId;

//    @NotNull
//    @Valid
//    @Schema(description = "Order items")
//    private List<OrderItemsDataDto> items;

}