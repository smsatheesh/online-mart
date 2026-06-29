package com.onlinemart.product.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "Product summary returned in browse results")
public class ProductBrowseResponseDto {

    @Schema(description = "ID of the category this product belongs to", example = "3")
    private Long categoryId;

    @Schema(description = "Unique product ID", example = "101")
    private Long productId;

    @Schema(description = "Name of the product", example = "Dell Inspiron 15")
    private String productName;

    @Schema(description = "Price of the product in smallest currency unit (paise/cents)", example = "55000")
    private Long price;

    @Schema(description = "Available stock quantity", example = "15")
    private Long availableQuantity;

    @Schema(description = "URL of the product thumbnail image", example = "/images/products/prod001.jpg")
    private String thumbnailUrl;

    @Schema(description = "Whether the product is active", example = "true")
    private Boolean status;

    @Schema(description = "ID of the user who created this product", example = "1001")
    private String createdBy;

    @Schema(description = "ID of the user who last updated this product", example = "1001")
    private String updatedBy;

    @Schema(description = "Timestamp when the product was created", example = "2026-06-22T10:00:00Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the product was last updated", example = "2026-06-22T10:00:00Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime updatedAt;
}