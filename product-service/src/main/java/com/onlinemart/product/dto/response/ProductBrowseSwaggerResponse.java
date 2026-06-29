package com.onlinemart.product.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Browse response containing a list of products")
public class ProductBrowseSwaggerResponse extends BrowseResponseDto<ProductBrowseResponseDto> {
    public ProductBrowseSwaggerResponse() {
        super(true, "Products fetched successfully", null, null);
    }
}