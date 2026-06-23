package com.onlinemart.product.service;

import com.onlinemart.product.dto.request.SaveProductRequestDto;
import com.onlinemart.product.dto.response.SaveProductResponseDto;

public interface ProductService {

    SaveProductResponseDto saveProduct(SaveProductRequestDto requestDto);

}