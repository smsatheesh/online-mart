package com.onlinemart.product.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Schema(description = "Request payload to create a new product")
public class CreateProductRequestDto implements ProductRequestDto {

    @NotBlank
    @Schema(description = "Name of the product", example = "Wireless Mouse")
    private String productName;

    @NotNull
    @Schema(description = "Category id for the product", example = "10")
    private Long categoryId;

    @Schema(description = "Product description")
    private String description;

    @NotNull
    @Schema(description = "Price in cents", example = "1999")
    private Long price;

    @NotNull
    @Schema(description = "Available quantity in stock", example = "50")
    private Long availableQuantity;

    @Schema(description = "Thumbnail image URL")
    private String thumbnailUrl;

    // Create DTO doesn't expose id in validation, but keep getter for interface
    public Long getId() {
        return null;
    }

}