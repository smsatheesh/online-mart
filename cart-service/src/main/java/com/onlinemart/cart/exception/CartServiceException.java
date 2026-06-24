package com.onlinemart.cart.exception;

import com.onlinemart.cart.dto.response.ErrorResponseDto;

public class CartServiceException extends RuntimeException {

    private final ErrorResponseDto errorResponse;

    public CartServiceException(ErrorResponseDto errorResponse) {
        super(errorResponse != null ? errorResponse.getMessage() : null);
        this.errorResponse = errorResponse;
    }

    public ErrorResponseDto getErrorResponse() {
        return errorResponse;
    }
}
