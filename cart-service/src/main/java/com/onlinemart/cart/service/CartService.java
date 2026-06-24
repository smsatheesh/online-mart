package com.onlinemart.cart.service;

import com.onlinemart.cart.dto.request.CartRequestDto;
import com.onlinemart.cart.dto.response.CartResponseDto;
import com.onlinemart.cart.dto.response.CartDetailResponseDto;

public interface CartService {

    CartResponseDto saveCart(CartRequestDto cart);

    CartDetailResponseDto fetchDetails(Long customerId);
}
