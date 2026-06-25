package com.onlinemart.cart.dto.request;

/**
 * Common interface for product create/update request DTOs.
 */
public interface CartRequestDto {

    Long getCustomerId();

    String getPlatform();

}