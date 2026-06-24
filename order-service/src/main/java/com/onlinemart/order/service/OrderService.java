package com.onlinemart.order.service;

import com.onlinemart.order.dto.request.CreateOrderRequestDto;
import com.onlinemart.order.dto.response.OrderResponseDto;

public interface OrderService {

    OrderResponseDto saveOrder(CreateOrderRequestDto requestDto);

}