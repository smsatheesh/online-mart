package com.onlinemart.order.service;

import com.onlinemart.order.repository.OrderItemRepository;

import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.onlinemart.order.dto.request.CreateOrderRequestDto;
import com.onlinemart.order.dto.request.OrderStatusRequestDto;
import com.onlinemart.order.dto.response.OrderDataDto;
import com.onlinemart.order.dto.response.OrderDetailResponseDto;
import com.onlinemart.order.dto.response.OrderDto;
import com.onlinemart.order.dto.response.OrderResponseDto;
import com.onlinemart.order.dto.response.ErrorResponseDto;
import com.onlinemart.order.entity.Order;
import com.onlinemart.order.entity.OrderItems;
import com.onlinemart.order.mapper.OrderMapper;
import com.onlinemart.order.event.OrderCreatedEvent;
import com.onlinemart.order.event.OrderEventPublisher;
import com.onlinemart.order.event.OrderItemEvent;
import com.onlinemart.order.repository.OrderRepository;
import com.onlinemart.order.exception.OrderServiceException;
import com.onlinemart.order.client.CartClientService;
import com.onlinemart.order.client.dto.response.CartItemsDataDto;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;
    private final OrderEventPublisher orderEventPublisher;
    private final CartClientService cartClientService;

    public OrderServiceImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository, OrderMapper orderMapper, OrderEventPublisher orderEventPublisher, CartClientService cartClientService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderMapper = orderMapper;
        this.orderEventPublisher = orderEventPublisher;
        this.cartClientService = cartClientService;
    }

    @Override
    @Transactional
    public OrderResponseDto saveOrder(CreateOrderRequestDto requestDto) {
        try {
            // 1. Fetch cart items from cart-service via Feign
            List<CartItemsDataDto> cartItems = cartClientService.fetchCartItems(requestDto.getCartId());

            if (cartItems.isEmpty()) {
                ErrorResponseDto error = ErrorResponseDto.builder()
                        .success(Boolean.FALSE)
                        .message("Cart is empty or not found")
                        .errorCode("ORDER_ITEM_NOT_FOUND")
                        .build();
                throw new OrderServiceException(error);
            }

            // 2. Calculate totalAmount from cart items
            long totalAmount = cartItems.stream()
                    .mapToLong(item -> item.getUnitPrice() * item.getQuantity())
                    .sum();

            // 3. Save the Order entity
            Order entity = orderMapper.toEntity(requestDto, totalAmount);
            Order savedOrder = orderRepository.save(entity);
            log.info("savedOrder id={} totalAmount={}", savedOrder.getId(), savedOrder.getTotalAmount());

            // 4. Map cart items → OrderItems and save
            List<OrderItems> orderItems = cartItems.stream()
                    .map(item -> {
                        OrderItems oi = new OrderItems();
                        oi.setOrderId(savedOrder.getId());
                        oi.setProductId(item.getProductId());
                        oi.setQuantity(item.getQuantity());
                        oi.setUnitPrice(item.getUnitPrice());
                        return oi;
                    }).toList();

            orderItemRepository.saveAll(orderItems);

            // 5. Build and publish order created event
            List<OrderItemEvent> itemEvents = orderItems.stream()
                    .map(oi -> new OrderItemEvent(
                            oi.getProductId(),
                            oi.getQuantity(),
                            oi.getUnitPrice()
                    ))
                    .toList();

            OrderCreatedEvent event = new OrderCreatedEvent(
                    savedOrder.getId(),
                    requestDto.getCustomerId(),
                    requestDto.getCartId(),
                    savedOrder.getTotalAmount(),
                    itemEvents
            );

            orderEventPublisher.publishOrderCreated(event);

            return orderMapper.toSaveResponseDto(savedOrder, orderItems);

        } catch (OrderServiceException ex) {
            throw ex;
        } catch (DataIntegrityViolationException ex) {
            log.error("Duplicate order item: {}", ex.getMessage());
            throw buildException("Item already exists in order", "DUPLICATE_ORDER_ITEM");
        } catch (Exception ex) {
            log.error("Unexpected error in saveOrder: {}", ex.getMessage(), ex);
            ErrorResponseDto error = ErrorResponseDto.builder()
                    .success(Boolean.FALSE)
                    .message("Failed to create order")
                    .errorCode("ORDER_SAVE_FAILED")
                    .build();
            throw new OrderServiceException(error);
        }
    }

    @Override
    public OrderDetailResponseDto fetchOrderDetails(Long orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> buildException("Order Id not exists", "ORDER_NOT_FOUND"));
            OrderDto orderDto = fetchOrder(order);

            return OrderDetailResponseDto.builder()
                    .success(true)
                    .message("Order Details fetched successfully")
                    .data(orderDto).build();
        } catch (OrderServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error in the fetchOrderDetails: {}", ex.getMessage(), ex);
            ErrorResponseDto error = ErrorResponseDto.builder()
                    .success(Boolean.FALSE)
                    .message("Failed to fetch order")
                    .errorCode("ORDER_FETCH_FAILED")
                    .build();
            throw new OrderServiceException(error);
        }
    }

    @Override
    public OrderDetailResponseDto updateStatusOfOrder(OrderStatusRequestDto requestDto) {
        try {
            Order order = orderRepository.findById(requestDto.getOrderId())
                    .orElseThrow(() -> buildException("Order Id not exists", "ORDER_NOT_FOUND"));

            order.setStatus(requestDto.getStatus());
            orderRepository.save(order);

            OrderDto orderDto = fetchOrder(order);

            return OrderDetailResponseDto.builder()
                    .success(Boolean.TRUE)
                    .message("Order status updated successfully")
                    .data(orderDto)
                    .build();

        } catch (OrderServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error in the updateStatusOfOrder: {}", ex.getMessage(), ex);
            ErrorResponseDto error = ErrorResponseDto.builder()
                    .success(Boolean.FALSE)
                    .message("Failed to update status of the order")
                    .errorCode("STATUS_UPDATE_FAILED")
                    .build();
            throw new OrderServiceException(error);
        }
    }

    private OrderDto fetchOrder(Order order) {

        Order existingOrder = order;
        List<OrderItems> allOrderItems = orderItemRepository.findByOrderId(existingOrder.getId());

        long totalPrice = allOrderItems.stream()
                .mapToLong(i -> (long) i.getQuantity() * i.getUnitPrice())
                .sum();

        OrderDto orderDto = new OrderDto();
        orderDto.setOrderId(existingOrder.getId());
        orderDto.setCustomerId(existingOrder.getCustomerId());
        orderDto.setCartId(existingOrder.getCartId());
        orderDto.setStatus(existingOrder.getStatus());
        orderDto.setTotalPrice(totalPrice);
        orderDto.setCreatedBy(existingOrder.getCreatedBy());
        orderDto.setCreatedAt(existingOrder.getCreatedAt());
        orderDto.setUpdatedBy(existingOrder.getUpdatedBy());
        orderDto.setUpdatedAt(existingOrder.getUpdatedAt());

        return orderDto;
    }

    private OrderServiceException buildException(String message, String errorCode) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .success(Boolean.FALSE)
                .message(message)
                .errorCode(errorCode)
                .build();
        return new OrderServiceException(error);
    }
}