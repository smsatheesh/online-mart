package com.onlinemart.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.onlinemart.order.client.dto.response.CartResponseDto;

@FeignClient(
        name = "cart-service",
        url = "${cart.service.url}",
        fallback = CartServiceFallback.class
)
public interface CartClient {

    @GetMapping("/api/carts/{cartId}")
    CartResponseDto fetchCart(@PathVariable("cartId") Long cartId);

}
