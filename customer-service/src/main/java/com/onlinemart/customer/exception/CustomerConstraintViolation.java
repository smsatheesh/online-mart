package com.onlinemart.customer.exception;

import java.util.Arrays;
import java.util.Optional;

public enum CustomerConstraintViolation {
    UNQ_USER_NAME("unq_customers_user_name", "USERNAME_ALREADY_EXISTS", "Username already exists"),
    UNQ_EMAIL("unq_customers_email", "EMAIL_ALREADY_EXISTS", "Email already exists"),
    UNQ_PHONE("unq_customers_phone_number", "PHONE_ALREADY_EXISTS", "Phone number already exists"),
    EMAIL_FORMAT_CHK("customers_email_format_chk", "INVALID_EMAIL_FORMAT", "Email format is invalid"),
    PHONE_FORMAT_CHK("customers_phone_format_chk", "INVALID_PHONE_FORMAT", "Phone number format is invalid"),
    UNQ_CUSTOMER_ADDRESS_TYPE("unq_customer_address_types", "DUPLICATE_ADDRESS_TYPE", "Address type already exists");

    private final String constraintName;
    private final String errorCode;
    private final String message;

    CustomerConstraintViolation(String constraintName, String errorCode, String message) {
        this.constraintName = constraintName;
        this.errorCode = errorCode;
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public static Optional<CustomerConstraintViolation> fromConstraintName(String name) {
        if (name == null) return Optional.empty();
        return Arrays.stream(values())
                .filter(v -> v.constraintName.equalsIgnoreCase(name))
                .findFirst();
    }
}