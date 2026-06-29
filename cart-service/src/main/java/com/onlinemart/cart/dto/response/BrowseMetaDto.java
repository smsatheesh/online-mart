package com.onlinemart.cart.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Pagination metadata returned with browse results")
public class BrowseMetaDto {

    @Schema(description = "Current page number (zero-based)", example = "0")
    private int page;

    @Schema(description = "Number of records per page", example = "20")
    private int size;

    @Schema(description = "Max records limit applied", example = "20")
    private int limit;

    @Schema(description = "Total records matching the filter", example = "54")
    private long totalElements;
}