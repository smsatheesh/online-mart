package com.onlinemart.product.dto.request;

/**
 * Common interface for product create/update request DTOs.
 */
public interface ProductRequestDto {

    Long getId();

    String getProductName();

    Long getCategoryId();

    String getDescription();

    Long getPrice();

    Long getAvailableQuantity();

    String getThumbnailUrl();

}
