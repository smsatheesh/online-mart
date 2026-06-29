package com.onlinemart.cart.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Browse response containing a list of carts")
public class CartBrowseSwaggerResponse extends BrowseResponseDto<CartBrowseResponseDto> {
    public CartBrowseSwaggerResponse() {
        super(true, "Carts fetched successfully", null, null);
    }
}