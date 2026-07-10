package com.onlinemart.cart.exception;

import com.onlinemart.cart.dto.response.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(CartServiceException.class)
    public ResponseEntity<ErrorResponseDto> handleCartServiceException(CartServiceException ex) {
        ErrorResponseDto err = ex.getErrorResponse();
        if (err == null) {
            err = ErrorResponseDto.builder()
                    .success(false)
                    .message("Internal server error")
                    .errorCode("INTERNAL_ERROR")
                    .build();
        }
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        // Map specific service error codes to appropriate HTTP statuses
        if (err.getErrorCode() != null && ("CART_NOT_FOUND".equalsIgnoreCase(err.getErrorCode()) || ("CART_ITEM_NOT_FOUND".equalsIgnoreCase(err.getErrorCode())))) {
            status = HttpStatus.NOT_FOUND;
        } else if (err.getErrorCode() != null && ("CART_ALREADY_EXISTS").equalsIgnoreCase(err.getErrorCode())) {
            status = HttpStatus.CONFLICT;
        }
        return new ResponseEntity<>(err, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ErrorResponseDto err = ErrorResponseDto.builder()
                .success(false)
                .message("Validation failed: " + details)
                .errorCode("VALIDATION_ERROR")
                .build();

        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGeneric(Exception ex) {
        ErrorResponseDto err = ErrorResponseDto.builder()
                .success(false)
                .message(ex.getMessage() != null ? ex.getMessage() : "Internal server error")
                .errorCode("INTERNAL_ERROR")
                .build();
        return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
