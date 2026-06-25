package com.onlinemart.order.mapper;

import org.springframework.stereotype.Component;

import com.onlinemart.order.dto.request.CreateOrderRequestDto;
import com.onlinemart.order.dto.response.OrderResponseDto;
import com.onlinemart.order.dto.response.OrderDataDto;
import com.onlinemart.order.entity.Order;

@Component
public class OrderMapper {

    public Order toEntity(CreateOrderRequestDto dto) {
        if (dto == null) return null;
        Order o = new Order();
        if (dto.getCustomerId() != null) {
            o.setCustomerId(dto.getCustomerId());
        }
        if (dto.getCartId() != null) {
            o.setCartId(dto.getCartId());
        }

        Long totalAmount = dto.getItems().stream()
                .mapToLong(item -> item.getUnitPrice() * item.getQuantity())
                .sum();

        o.setTotalAmount(totalAmount);
        o.setStatus("PLACED");
        return o;
    }

    public OrderResponseDto toSaveResponseDto(Order order) {
        if (order == null) return null;
        OrderDataDto data = OrderDataDto.builder()
                .orderId(null)
                .customerId(order.getCustomerId())
                .cartId(order.getCartId())
                .status(order.getStatus() != null ? order.getStatus() : null)
                .items(null)
                .totalPrice(null)
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

    public OrderResponseDto to(Order order) {
        if (order == null) return null;
        OrderDataDto data = OrderDataDto.builder()
                .orderId(null)
                .customerId(order.getCustomerId())
                .cartId(order.getCartId())
                .status(order.getStatus() != null ? order.getStatus() : null)
                .items(null)
                .totalPrice(null)
                .createdBy(order.getCreatedBy())
                .createdAt(order.getCreatedAt())
                .updatedBy(order.getUpdatedBy())
                .updatedAt(order.getUpdatedAt())
                .build();

        return OrderResponseDto.builder()
                .success(Boolean.TRUE)
                .message("Order fetched successfully")
                .data(data)
                .build();
    }

    public OrderDataDto toDetailResponse(Order order) {
        if (order == null) return null;
        OrderDataDto data = OrderDataDto.builder()
                .orderId(null)
                .customerId(order.getCustomerId())
                .cartId(order.getCartId())
                .status(order.getStatus() != null ? order.getStatus() : null)
                .items(null)
                .totalPrice(null)
                .createdBy(order.getCreatedBy())
                .createdAt(order.getCreatedAt())
                .updatedBy(order.getUpdatedBy())
                .updatedAt(order.getUpdatedAt())
                .build();

        return data;
    }

}