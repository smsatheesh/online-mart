package com.onlinemart.product.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
@Schema(description = "Generic browse response wrapper")
public class BrowseResponseDto<T> {

    @Schema(description = "Whether the request was successful", example = "true")
    private boolean success;

    @Schema(description = "Human-readable status message", example = "Products fetched successfully")
    private String message;

    @Schema(description = "List of result items")
    private List<T> data;

    @Schema(description = "Pagination metadata")
    private BrowseMetaDto meta;
}