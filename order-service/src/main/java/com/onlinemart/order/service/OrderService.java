package com.onlinemart.order.service;

import com.onlinemart.order.dto.request.CreateOrderRequestDto;
import com.onlinemart.order.dto.response.OrderResponseDto;
import com.onlinemart.order.dto.response.OrderDetailResponseDto;
import com.onlinemart.order.dto.response.BrowseResponseDto;
import com.onlinemart.order.dto.response.OrderBrowseResponseDto;
import com.onlinemart.order.dto.response.OrderBrowseResponseDto;
import com.onlinemart.order.dto.request.OrderStatusRequestDto;
import com.onlinemart.order.dto.request.BrowseRequestDto;
import com.onlinemart.order.event.OrderFailedItemEvent;

import java.util.*;

public interface OrderService {

    OrderResponseDto saveOrder(CreateOrderRequestDto requestDto);

    OrderDetailResponseDto fetchOrderDetails(Long orderId);

    OrderDetailResponseDto updateStatusOfOrder(OrderStatusRequestDto requestDto);

    List<OrderFailedItemEvent> getOrderItems(Long orderId);

    BrowseResponseDto<OrderBrowseResponseDto> browse(BrowseRequestDto req);

}