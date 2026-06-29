package com.onlinemart.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Browse response containing a list of products")
public class OrderBrowseSwaggerResponse extends BrowseResponseDto<OrderBrowseResponseDto> {
    public OrderBrowseSwaggerResponse() {
        super(true, "Orders fetched successfully", null, null);
    }
}