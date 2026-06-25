package com.onlinemart.product.dto.request;

import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Schema(description = "Request payload to create a new product")
public class UpdateProductRequestDto implements ProductRequestDto {

    @Schema(description = "Identifier of the product for update flow", example = "Wireless Mouse")
    private Long id;

    @Schema(description = "Name of the product", example = "Wireless Mouse")
    private String productName;

    @Schema(description = "Category id for the product", example = "10")
    private Long categoryId;

    @Schema(description = "Product description")
    private String description;

    @Schema(description = "Price in cents", example = "1999")
    private Long price;

    @Schema(description = "Available quantity in stock", example = "50")
    private Long availableQuantity;

    @Schema(description = "Thumbnail image URL")
    private String thumbnailUrl;

}