package com.onlinemart.customer.exception;

import com.onlinemart.customer.dto.response.ErrorResponseDto;

public class CustomerServiceException extends RuntimeException {

    private final ErrorResponseDto errorResponse;

    public CustomerServiceException(ErrorResponseDto errorResponse) {
        super(errorResponse != null ? errorResponse.getMessage(): null);
        this.errorResponse = errorResponse;
    }

    public ErrorResponseDto getErrorResponse() {
        return errorResponse;
    }
}