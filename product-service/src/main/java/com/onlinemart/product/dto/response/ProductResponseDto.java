package com.onlinemart.product.dto.response;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@SuperBuilder
@Schema(description = "Response returned after fetching details of the product")
public class ProductResponseDto extends AuditResponseDto {

    @Schema(description = "Internal database id", example = "100")
    private Long id;

    @Schema(description = "Category id", example = "10")
    private Long categoryId;

    @Schema(description = "Product id exposed to clients", example = "200")
    private Long productId;

    @Schema(description = "Name of the product", example = "Wireless Mouse")
    private String productName;

    @Schema(description = "Price in cents", example = "1999")
    private Long price;

    @Schema(description = "Available stock quantity", example = "50")
    private Long availableQuantity;

    @Schema(description = "URL to thumbnail image")
    private String thumbnailUrl;

    @Schema(description = "Status of the product (active/inactive)", example = "true")
    private Boolean status;

}