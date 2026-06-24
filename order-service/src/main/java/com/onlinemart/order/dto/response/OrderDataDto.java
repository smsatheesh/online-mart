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
public class OrderDataDto extends AuditResponseDto {

    @Schema(description = "Order identifier", example = "1")
    private Long orderId;

    @Schema(description = "Customer identifier", example = "1")
    private Long customerId;

    @Schema(description = "Cart identifier", example = "13")
    private Long cartId;

    @Schema(description = "Status of the order", example = "100")
    private String status;

    @Schema(description = "Total value of the order", example = "2000")
    private Long totalPrice;

    @Schema(description = "Order items")
    private OrderItemsDataDto[] items;
}
