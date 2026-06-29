package com.onlinemart.cart.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "Request payload for browsing carts with pagination, sorting, and filtering")
public class BrowseRequestDto {

    @Schema(description = "Zero-based page number", example = "0", defaultValue = "0")
    private int page = 0;

    @Schema(description = "Number of records per page", example = "20", defaultValue = "20")
    private int size = 20;

    @Schema(description = "Max records to return", example = "20", defaultValue = "20")
    private int limit = 20;

    @Schema(
            description = "List of filter conditions. Each entry is a field-value pair.",
            example = "[{\"customerId\": 1}, {\"status\": true}]"
    )
    private List<Map<String, Object>> filters;

    @Schema(
            description = "List of sort conditions. Each entry is field mapped to 'asc' or 'desc'.",
            example = "[{\"createdAt\": \"desc\"}]"
    )
    private List<Map<String, String>> sort;
}