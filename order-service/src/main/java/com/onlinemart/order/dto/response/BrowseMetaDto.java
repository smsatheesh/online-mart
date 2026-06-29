package com.onlinemart.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Pagination metadata returned with browse results")
public class BrowseMetaDto {

    @Schema(description = "Current page number (zero-based)", example = "0")
    private int page;

    @Schema(description = "Number of records requested per page", example = "20")
    private int size;

    @Schema(description = "Max records limit applied", example = "20")
    private int limit;

    @Schema(description = "Total number of records matching the filter", example = "142")
    private long totalElements;
}