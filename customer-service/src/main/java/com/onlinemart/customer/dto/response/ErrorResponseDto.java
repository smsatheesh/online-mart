package com.onlinemart.customer.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Schema(description = "Standard error response following API contract")
public class ErrorResponseDto {

    @Schema(description = "Indicates operation success", example = "false")
    private boolean success;

    @Schema(description = "Short error message", example = "Validation Failed")
    private String message;

    @Schema(description = "Application specific error code", example = "ORDER_SAVE_ERROR")
    private String errorCode;

}
