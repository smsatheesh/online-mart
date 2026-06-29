package com.onlinemart.cart.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Generic browse response wrapper")
public class BrowseResponseDto<T> {

    @Schema(description = "Whether the request was successful", example = "true")
    private boolean success;

    @Schema(description = "Human-readable status message", example = "Carts fetched successfully")
    private String message;

    @Schema(description = "List of result items")
    private List<T> data;

    @Schema(description = "Pagination metadata")
    private BrowseMetaDto meta;
}