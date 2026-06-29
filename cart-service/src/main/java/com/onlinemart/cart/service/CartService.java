package com.onlinemart.cart.service;

import com.onlinemart.cart.dto.request.CartRequestDto;
import com.onlinemart.cart.dto.request.CreateCartItemRequestDto;
import com.onlinemart.cart.dto.request.UpdateCartItemRequestDto;
import com.onlinemart.cart.dto.request.BrowseRequestDto;
import com.onlinemart.cart.dto.response.CartResponseDto;
import com.onlinemart.cart.dto.response.CartDetailResponseDto;
import com.onlinemart.cart.dto.response.BrowseResponseDto;
import com.onlinemart.cart.dto.response.CartBrowseResponseDto;

import java.util.List;

public interface CartService {

    CartResponseDto saveCart(CartRequestDto cart);

    CartDetailResponseDto fetchDetails(Long customerId);

    CartResponseDto saveCartItems(CreateCartItemRequestDto cartItem);

    CartResponseDto updateCartItems(UpdateCartItemRequestDto cartItem);

    void removeCartItem(Long cartId, Long itemId);

    void clearCartItems(Long cartId);

    CartResponseDto fetchCartAndDetails(Long cartId);

    BrowseResponseDto<CartBrowseResponseDto> browse(BrowseRequestDto req);

}
