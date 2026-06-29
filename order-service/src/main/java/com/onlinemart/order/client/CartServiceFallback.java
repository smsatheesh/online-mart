package com.onlinemart.order.client;

import com.onlinemart.order.client.dto.response.CartResponseDto;
import com.onlinemart.order.exception.CartServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CartServiceFallback implements CartClient {

    private static final Logger log = LoggerFactory.getLogger(CartServiceFallback.class);

    @Override
    public CartResponseDto fetchCart(Long cartId) {
        log.error("CartServiceFallback triggered for cartId={} — cart-service is unavailable", cartId);
        throw new CartServiceUnavailableException(
                "Cart service is currently unavailable. Please try again later."
        );
    }
}