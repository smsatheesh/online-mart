package com.onlinemart.product.exception;

import com.onlinemart.product.dto.response.ErrorResponseDto;

public class ProductServiceException extends RuntimeException {

    private final ErrorResponseDto errorResponse;

    public ProductServiceException(ErrorResponseDto errorResponse) {
        super(errorResponse != null ? errorResponse.getMessage() : null);
        this.errorResponse = errorResponse;
    }

    public ErrorResponseDto getErrorResponse() {
        return errorResponse;
    }
}
