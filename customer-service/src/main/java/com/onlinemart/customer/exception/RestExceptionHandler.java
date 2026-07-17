package com.onlinemart.customer.exception;

import com.onlinemart.customer.dto.response.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Set;
import java.util.stream.Collectors;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(CustomerServiceException.class)
    public ResponseEntity<ErrorResponseDto> handleCartServiceException(CustomerServiceException ex) {
        ErrorResponseDto err = ex.getErrorResponse();
        if (err == null) {
            err = ErrorResponseDto.builder()
                    .success(false)
                    .message("Internal server error")
                    .errorCode("INTERNAL_ERROR")
                    .build();
        }

        HttpStatus status = resolveStatus(err.getErrorCode());
        return new ResponseEntity<>(err, status);
    }

    private static final Set<String> NOT_FOUND_CODES = Set.of(
            "CUSTOMER_NOT_FOUND",
            "CUSTOMER_DETAILS_NOT_FOUND",
            "CUSTOMER_ADDRESS_NOT_FOUND"
    );

    private static final Set<String> CONFLICT_CODES = Set.of(
            "USERNAME_ALREADY_EXISTS",
            "EMAIL_ALREADY_EXISTS",
            "PHONE_ALREADY_EXISTS",
            "DUPLICATE_ID",
            "CUSTOMER_ALREADY_INACTIVE",
            "DUPLICATE_ADDRESS_TYPE"
    );

    private static final Set<String> BAD_REQUEST_CODES = Set.of(
            "INVALID_EMAIL_FORMAT",
            "INVALID_PHONE_FORMAT",
            "INVALID_CONTACT",
            "CUSTOMER_DATA_CONFLICT" // fallback from unmapped constraint, treat as 400 or 409 depending on your preference
    );

    private HttpStatus resolveStatus(String errorCode) {
        if (errorCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        if (NOT_FOUND_CODES.contains(errorCode)) {
            return HttpStatus.NOT_FOUND;
        }
        if (CONFLICT_CODES.contains(errorCode)) {
            return HttpStatus.CONFLICT;
        }
        if (BAD_REQUEST_CODES.contains(errorCode)) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
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
