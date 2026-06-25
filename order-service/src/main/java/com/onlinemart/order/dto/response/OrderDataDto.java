package com.onlinemart.order.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "Order Data")
public class OrderDataDto extends OrderDto {

    @Schema(description = "Order items")
    private OrderItemsDataDto[] items;
}
