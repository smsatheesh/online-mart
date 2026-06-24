package com.onlinemart.order.service;

import com.onlinemart.order.repository.OrderItemRepository;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.onlinemart.order.dto.request.CreateOrderRequestDto;
import com.onlinemart.order.dto.response.OrderResponseDto;
import com.onlinemart.order.entity.Order;
import com.onlinemart.order.entity.OrderItems;
import com.onlinemart.order.mapper.OrderMapper;
import com.onlinemart.order.repository.OrderRepository;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;

    public OrderServiceImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderMapper = orderMapper;
    }

    @Override
    @Transactional
    public OrderResponseDto saveOrder(CreateOrderRequestDto requestDto) {
        try {
            Order entity = orderMapper.toEntity(requestDto);
            Order saved = orderRepository.save(entity);

            List<OrderItems> orderItems = requestDto.getItems().stream()
                    .map(item -> {
                        OrderItems oi = new OrderItems();
                        oi.setOrderId(saved.getId());
                        oi.setProductId(item.getProductId());
                        oi.setQuantity(item.getQuantity());
                        return oi;
                    }).toList();

            orderItemRepository.saveAll(orderItems);

            return orderMapper.toSaveResponseDto(saved);
        } catch (Exception ex) {
            throw ex;
        }
    }

}