package com.onlinemart.cart.service;

import org.springframework.stereotype.Service;

import com.onlinemart.cart.dto.request.CartRequestDto;
import com.onlinemart.cart.dto.request.CreateCartItemRequestDto;
import com.onlinemart.cart.dto.request.UpdateCartItemRequestDto;
import com.onlinemart.cart.dto.response.CartResponseDto;
import com.onlinemart.cart.dto.response.CartDataDto;
import com.onlinemart.cart.dto.response.CartDetailResponseDto;
import com.onlinemart.cart.dto.response.CartItemsDataDto;
import com.onlinemart.cart.dto.response.ErrorResponseDto;
import com.onlinemart.cart.entity.Cart;
import com.onlinemart.cart.entity.CartItems;
import com.onlinemart.cart.exception.CartServiceException;
import com.onlinemart.cart.mapper.CartMapper;
import com.onlinemart.cart.repository.CartRepository;
import com.onlinemart.cart.repository.CartItemRepository;
import com.onlinemart.cart.client.ProductClient;
import com.onlinemart.cart.client.dto.ProductResponseDto;
import com.onlinemart.cart.client.dto.ProductDataDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.*;

@Slf4j
@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final CartItemRepository cartItemRepository;
    private final ProductClient productClient;

    public CartServiceImpl(CartRepository cartRepository, CartMapper cartMapper,
                           CartItemRepository cartItemRepository, ProductClient productClient) {
        this.cartRepository = cartRepository;
        this.cartMapper = cartMapper;
        this.cartItemRepository = cartItemRepository;
        this.productClient = productClient;
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

    @Override
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

    @Override
    public CartResponseDto saveCartItems(CreateCartItemRequestDto requestDto) {
        try {
            if (requestDto == null) {
                throw buildException("Invalid request", "INVALID_REQUEST");
            }

            Long cartId = requestDto.getCartId();
            if (cartId == null) {
                throw buildException("Cart id is required", "CART_ID_REQUIRED");
            }

            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> buildException("Cart not found for id: " + cartId, "CART_NOT_FOUND"));

            Long productId = requestDto.getProductId();
            ProductResponseDto prodResp = productClient.fetchProduct(productId);
            if (prodResp == null || prodResp.getData() == null) {
                throw buildException("Product not found for id: " + productId, "PRODUCT_NOT_FOUND");
            }

            ProductDataDto prod = prodResp.getData();
            Long price = prod.getPrice();
            if (price == null) {
                throw buildException("Product price missing for id: " + productId, "PRODUCT_PRICE_MISSING");
            }

            CartItems item = new CartItems();
            item.setCartId(cartId);
            item.setProductId(productId);
            item.setQuantity(requestDto.getQuantity());
            item.setUnitPrice(price);

            cartItemRepository.save(item);

            // Fetch all items for this cart and calculate total
            CartDataDto data = fetchAllItems(cartId, cart);

            return CartResponseDto.builder()
                    .success(Boolean.TRUE)
                    .message("Item added to cart successfully")
                    .data(data)
                    .build();

        } catch (CartServiceException ex) {
            throw ex;
        } catch (DataIntegrityViolationException ex) {
            log.error("Duplicate cart item: {}", ex.getMessage());
            throw buildException("Product already exists in cart", "DUPLICATE_CART_ITEM");
        } catch (Exception ex) {
            log.error("Unexpected error in saveCartItems: {}", ex.getMessage(), ex);
            throw buildException("Failed to save cart item", "CART_ITEM_SAVE_ERROR");
        }
    }

    @Override
    public CartResponseDto updateCartItems(UpdateCartItemRequestDto requestDto) {
        try {
            Cart cart = cartRepository.findById(requestDto.getCartId())
                    .orElseThrow(
                            () -> buildException("Cart not found for id: " + requestDto.getCartId(), "CART_NOT_FOUND"));

            CartItems items = cartItemRepository.findById(requestDto.getItemId())
                    .map(existing -> {
                        existing.setQuantity(requestDto.getQuantity());
                        return existing;
                    })
                    .orElseThrow(
                            () -> buildException("Cart Item not found for id: " + requestDto.getItemId(), "CART_ITEM_NOT_FOUND"));

            cartItemRepository.save(items);


            CartDataDto data = fetchAllItems(requestDto.getCartId(), cart);

            return CartResponseDto.builder()
                    .success(Boolean.TRUE)
                    .message("Cart item updated successfully")
                    .data(data)
                    .build();

        } catch (CartServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            ErrorResponseDto error = ErrorResponseDto.builder()
                    .success(false)
                    .message("Failed to update cart items")
                    .errorCode("CART_ITEM_UPDATE_FAILED")
                    .build();
            throw new CartServiceException(error);
        }
    }

    private CartDataDto fetchAllItems(Long cartId, Cart cart) {
        // Fetch all items for this cart and calculate total
        List<CartItems> allItems = cartItemRepository.findByCartId(cartId);
        long cartTotal = allItems.stream()
                .mapToLong(i -> (long) i.getQuantity() * i.getUnitPrice())
                .sum();

        CartItemsDataDto[] itemDtos = allItems.stream()
                .map(i -> CartItemsDataDto.builder()
                        .itemId(i.getId())
                        .productId(i.getProductId())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .build())
                .toArray(CartItemsDataDto[]::new);

        CartDataDto data = new CartDataDto();
        data.setCartId(cart.getId());
        data.setCustomerId(cart.getCustomerId());
        data.setPlatform(cart.getPlatform());
        data.setStatus(cart.getStatus());
        data.setCartTotal(cartTotal);
        data.setItems(itemDtos);
        data.setCreatedBy(cart.getCreatedBy());
        data.setCreatedAt(cart.getCreatedAt());
        data.setUpdatedBy(cart.getUpdatedBy());
        data.setUpdatedAt(cart.getUpdatedAt());

        return data;
    }

    private CartServiceException buildException(String message, String errorCode) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
        return new CartServiceException(error);
    }

}