package com.onlinemart.order.mapper;

import org.springframework.stereotype.Component;
import java.util.List;

import com.onlinemart.order.dto.request.CreateOrderRequestDto;
import com.onlinemart.order.dto.response.OrderResponseDto;
import com.onlinemart.order.dto.response.OrderDataDto;
import com.onlinemart.order.dto.response.OrderItemsDataDto;
import com.onlinemart.order.entity.Order;
import com.onlinemart.order.entity.OrderItems;

@Component
public class OrderMapper {

    public Order toEntity(CreateOrderRequestDto dto, Long totalAmount) {
        if (dto == null) return null;
        Order o = new Order();

        if (dto.getCustomerId() != null) {
            o.setCustomerId(dto.getCustomerId());
        }

        if (dto.getCartId() != null) {
            o.setCartId(dto.getCartId());
        }

        o.setTotalAmount(totalAmount);
        o.setStatus("PENDING");
        return o;
    }

    public OrderResponseDto toSaveResponseDto(Order order, List<OrderItems> orderItems) {
        if (order == null) return null;

        OrderItemsDataDto[] itemsArray = orderItems.stream()
                .map(oi -> OrderItemsDataDto.builder()
                        .itemId(oi.getId())
                        .productId(oi.getProductId())
                        .quantity(oi.getQuantity())
                        .unitPrice(oi.getUnitPrice())
                        .build())
                .toArray(OrderItemsDataDto[]::new);

        OrderDataDto data = OrderDataDto.builder()
                .orderId(order.getId())
                .customerId(order.getCustomerId())
                .cartId(order.getCartId())
                .status(order.getStatus())
                .totalPrice(order.getTotalAmount())
                .items(itemsArray)
                .createdBy(order.getCreatedBy())
                .createdAt(order.getCreatedAt())
                .updatedBy(order.getUpdatedBy())
                .updatedAt(order.getUpdatedAt())
                .build();

        return OrderResponseDto.builder()
                .success(Boolean.TRUE)
                .message("Order created successfully")
                .data(data)
                .build();
    }

}