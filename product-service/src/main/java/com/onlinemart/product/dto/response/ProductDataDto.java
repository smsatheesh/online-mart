package com.onlinemart.product.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "Detailed product payload returned in response.data")
public class ProductDataDto extends AuditResponseDto {

    @Schema(description = "Category identifier", example = "CAT001")
    private Long categoryId;

    @Schema(description = "Product id exposed to clients", example = "PROD001")
    private Long productId;

    @Schema(description = "Name of the product", example = "Dell Inspiron 15")
    private String productName;

    @Schema(description = "Price in the smallest currency unit", example = "55000")
    private Long price;

    @Schema(description = "Available stock quantity", example = "15")
    private Long availableQuantity;

    @Schema(description = "URL to thumbnail image")
    private String thumbnailUrl;

    @Schema(description = "Status of the product (active/inactive)", example = "true")
    private Boolean status;

}
