package com.onlinemart.order.exception;

import com.onlinemart.order.dto.response.ErrorResponseDto;

public class OrderServiceException extends RuntimeException {

    private final ErrorResponseDto errorResponse;

    public OrderServiceException(ErrorResponseDto errorResponse) {
        super(errorResponse != null ? errorResponse.getMessage() : null);
        this.errorResponse = errorResponse;
    }

    public ErrorResponseDto getErrorResponse() {
        return errorResponse;
    }
}
