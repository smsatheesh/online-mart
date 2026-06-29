package com.onlinemart.order.client;

import org.springframework.stereotype.Component;

import com.onlinemart.order.client.dto.response.CartItemsDataDto;
import com.onlinemart.order.client.dto.response.CartResponseDto;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class CartClientService {

    private final CartClient cartClient;

    public CartClientService(CartClient cartClient) {
        this.cartClient = cartClient;
    }

    public List<CartItemsDataDto> fetchCartItems(Long cartId) {
        CartResponseDto response = cartClient.fetchCart(cartId);
        if (response == null || response.getData() == null || response.getData().getItems() == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(response.getData().getItems());
    }

}
