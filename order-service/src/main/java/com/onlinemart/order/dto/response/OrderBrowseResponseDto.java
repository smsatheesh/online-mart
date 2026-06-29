package com.onlinemart.order.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "Order summary returned in browse results")
public class OrderBrowseResponseDto {

    @Schema(description = "Unique order ID", example = "201")
    private Long orderId;

    @Schema(description = "ID of the customer who placed the order", example = "1001")
    private Long customerId;

    @Schema(description = "ID of the cart associated with this order", example = "301")
    private Long cartId;

    @Schema(description = "Current status of the order", example = "PLACED")
    private String status;

    @Schema(description = "Total order amount", example = "55000")
    private Long totalAmount;

    @Schema(description = "ID of user who created this order", example = "1001")
    private Long createdBy;

    @Schema(description = "ID of user who last updated this order", example = "1001")
    private Long updatedBy;

    @Schema(description = "Created timestamp", example = "2026-06-22T10:00:00Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;

    @Schema(description = "Updated timestamp", example = "2026-06-22T10:00:00Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime updatedAt;
}