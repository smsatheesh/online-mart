package com.onlinemart.cart.service;

import org.springframework.stereotype.Service;

import com.onlinemart.cart.dto.request.CartRequestDto;
import com.onlinemart.cart.dto.response.CartResponseDto;
import com.onlinemart.cart.dto.response.CartDetailResponseDto;
import com.onlinemart.cart.dto.response.ErrorResponseDto;
import com.onlinemart.cart.entity.Cart;
import com.onlinemart.cart.exception.CartServiceException;
import com.onlinemart.cart.mapper.CartMapper;
import com.onlinemart.cart.repository.CartRepository;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;

    public CartServiceImpl(CartRepository cartRepository, CartMapper cartMapper) {
        this.cartRepository = cartRepository;
        this.cartMapper = cartMapper;
    }

    @Override
    public CartResponseDto saveCart(CartRequestDto requestDto) {
        try {
            Cart cart = cartMapper.toEntity(requestDto);
            Cart saved = cartRepository.save(cart);
            return cartMapper.toSaveResponseDto(saved);
        } catch (CartServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            ErrorResponseDto error = ErrorResponseDto.builder()
                    .success(false)
                    .message("Failed to save cart")
                    .errorCode("CART_SAVE_ERROR")
                    .build();
            throw new CartServiceException(error);
        }
    }

    public CartDetailResponseDto fetchDetails(Long customerId) {
        try {
            return cartRepository.findByCustomerId(customerId)
                    .map(cartMapper::toDetailResponse)
                    .orElseThrow(() -> {
                        ErrorResponseDto error = ErrorResponseDto.builder()
                                .success(false)
                                .message("Cart not found for customer: " + customerId)
                                .errorCode("CART_NOT_FOUND")
                                .build();
                        return new CartServiceException(error);
                    });
        } catch (Exception ex) {
            ErrorResponseDto error = ErrorResponseDto.builder()
                    .success(false)
                    .message("Failed to fetch cart")
                    .errorCode("CART_FETCH_FAILED")
                    .build();
            throw new CartServiceException(error);
        }
    }

}