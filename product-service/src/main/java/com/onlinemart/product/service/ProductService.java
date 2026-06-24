package com.onlinemart.product.service;

import com.onlinemart.product.dto.request.ProductRequestDto;
import com.onlinemart.product.dto.response.ProductResponseDto;

public interface ProductService {

    ProductResponseDto saveProduct(ProductRequestDto requestDto);

    ProductResponseDto fetchProduct(Long id);
}