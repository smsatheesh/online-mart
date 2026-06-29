package com.onlinemart.cart.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Cart summary returned in browse results")
public class CartBrowseResponseDto {

    @Schema(description = "Unique cart ID", example = "301")
    private Long cartId;

    @Schema(description = "ID of the customer who owns this cart", example = "1001")
    private Long customerId;

    @Schema(description = "Platform the cart was created on", example = "WEB")
    private String platform;

    @Schema(description = "Whether the cart is active", example = "true")
    private Boolean status;

    @Schema(description = "ID of the user who created this cart", example = "1001")
    private Long createdBy;

    @Schema(description = "ID of the user who last updated this cart", example = "1001")
    private Long updatedBy;

    @Schema(description = "Timestamp when the cart was created", example = "2026-06-22T10:00:00Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the cart was last updated", example = "2026-06-22T10:00:00Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime updatedAt;
}