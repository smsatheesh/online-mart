package com.onlinemart.cart.mapper;

import com.onlinemart.cart.dto.request.CartRequestDto;
import com.onlinemart.cart.dto.response.CartBrowseResponseDto;
import com.onlinemart.cart.dto.response.CartDataDto;
import com.onlinemart.cart.dto.response.CartDetailResponseDto;
import com.onlinemart.cart.dto.response.CartDto;
import com.onlinemart.cart.dto.response.CartResponseDto;
import com.onlinemart.cart.entity.Cart;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Arrays;

@Component
public class CartMapper {

    public Cart toEntity(CartRequestDto dto) {
        if (dto == null) return null;
        Cart c = new Cart();
        if (dto.getCustomerId() != null) {
            c.setCustomerId(dto.getCustomerId());
        }
        c.setPlatform(dto.getPlatform());
        c.setStatus(Boolean.TRUE);
        return c;
    }

    public CartResponseDto toSaveResponseDto(Cart cart) {
        if (cart == null) return null;
        CartDataDto data = new CartDataDto();
        data.setCartId(cart.getId() != null ? cart.getId() : null);
        data.setCustomerId(cart.getCustomerId());
        data.setPlatform(cart.getPlatform());
        data.setStatus(cart.getStatus());
        data.setCartTotal(null);
        data.setItems(null);
        data.setCreatedBy(cart.getCreatedBy());
        data.setCreatedAt(cart.getCreatedAt());
        data.setUpdatedBy(cart.getUpdatedBy());
        data.setUpdatedAt(cart.getUpdatedAt());

        return CartResponseDto.builder()
                .success(Boolean.TRUE)
                .message("Cart created successfully")
                .data(data)
                .build();
    }

    public CartResponseDto to(Cart cart) {
        if (cart == null) return null;
        CartDataDto data = new CartDataDto();
        data.setCartId(cart.getId() != null ? cart.getId() : null);
        data.setCustomerId(cart.getCustomerId());
        data.setPlatform(cart.getPlatform());
        data.setStatus(cart.getStatus());
        data.setCartTotal(null);
        data.setItems(null);
        data.setCreatedBy(cart.getCreatedBy());
        data.setCreatedAt(cart.getCreatedAt());
        data.setUpdatedBy(cart.getUpdatedBy());
        data.setUpdatedAt(cart.getUpdatedAt());

        return CartResponseDto.builder()
                .success(Boolean.TRUE)
                .message("Cart fetched successfully")
                .data(data)
                .build();
    }

    public CartDetailResponseDto toDetailResponse(List<Cart> carts) {
        if (carts == null || carts.isEmpty()) {
            return null;
        }

        List<CartDto> data = carts.stream()
                .map(cart -> {
                    CartDto dto = new CartDto();
                    dto.setCartId(cart.getId());
                    dto.setCustomerId(cart.getCustomerId());
                    dto.setPlatform(cart.getPlatform());
                    dto.setStatus(cart.getStatus());
                    dto.setCartTotal(null);
                    dto.setCreatedBy(cart.getCreatedBy());
                    dto.setCreatedAt(cart.getCreatedAt());
                    dto.setUpdatedBy(cart.getUpdatedBy());
                    dto.setUpdatedAt(cart.getUpdatedAt());
                    return dto;
                })
                .toList();

        return CartDetailResponseDto.builder()
                .success(Boolean.TRUE)
                .message("Cart fetched successfully")
                .data(data)
                .build();
    }

    public CartBrowseResponseDto toCartBrowseDto(Cart cart) {
        CartBrowseResponseDto dto = new CartBrowseResponseDto();
        dto.setCartId(cart.getId());
        dto.setCustomerId(cart.getCustomerId());
        dto.setPlatform(cart.getPlatform());
        dto.setStatus(cart.getStatus());
        dto.setCreatedBy(cart.getCreatedBy());
        dto.setUpdatedBy(cart.getUpdatedBy());
        dto.setCreatedAt(cart.getCreatedAt());
        dto.setUpdatedAt(cart.getUpdatedAt());
        return dto;
    }

}