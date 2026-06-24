package com.onlinemart.product.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Detailed cart availability payload returned in response.data")
public class AvailabilityDataDto {

    @Schema(description = "Product identifier", example = "1")
    private Long productId;

    @Schema(description = "Is the product available?", example = "true")
    private Boolean isAvailable;

    @Schema(description = "Quantity available for the product user trying to check", example = "18")
    private Long availableQuantity;

}