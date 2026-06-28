package com.onlinemart.product.service;

import com.onlinemart.product.dto.request.ProductRequestDto;
import com.onlinemart.product.dto.response.ProductResponseDto;
import com.onlinemart.product.dto.response.AvailabilityResponseDto;

public interface ProductService {

    ProductResponseDto saveProduct(ProductRequestDto requestDto);

    ProductResponseDto fetchProduct(Long productId);

    AvailabilityResponseDto checkAvailability(Long productId);

    void deductStock(Long productId, Long quantity, Long orderId, Long cartId);

    void restoreStock(Long productId, Long quantity);

}