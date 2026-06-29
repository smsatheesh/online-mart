package com.onlinemart.product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "Request payload for browsing products with pagination, sorting, and filtering")
public class BrowseRequestDto {

    @Schema(description = "Zero-based page number", example = "0", defaultValue = "0")
    private int page = 0;

    @Schema(description = "Number of records per page", example = "20", defaultValue = "20")
    private int size = 20;

    @Schema(description = "Max records to return (used alongside size)", example = "20", defaultValue = "20")
    private int limit = 20;

    @Schema(
            description = "List of filter conditions. Each entry is a key-value pair where key is the field name and value is the filter value.",
            example = "[{\"categoryId\": 1}, {\"status\": true}]"
    )
    private List<Map<String, Object>> filters;

    @Schema(
            description = "List of sort conditions. Each entry is a key-value pair where key is the field name and value is 'asc' or 'desc'.",
            example = "[{\"price\": \"asc\"}, {\"createdAt\": \"desc\"}]"
    )
    private List<Map<String, String>> sort;
}