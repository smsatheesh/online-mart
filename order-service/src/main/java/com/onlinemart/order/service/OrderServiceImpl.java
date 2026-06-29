package com.onlinemart.order.service;

import com.onlinemart.order.repository.OrderItemRepository;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.onlinemart.order.dto.request.CreateOrderRequestDto;
import com.onlinemart.order.dto.request.OrderStatusRequestDto;
import com.onlinemart.order.dto.request.BrowseRequestDto;
import com.onlinemart.order.dto.response.OrderDetailResponseDto;
import com.onlinemart.order.dto.response.OrderDto;
import com.onlinemart.order.dto.response.OrderResponseDto;
import com.onlinemart.order.dto.response.ErrorResponseDto;
import com.onlinemart.order.dto.response.BrowseResponseDto;
import com.onlinemart.order.dto.response.OrderBrowseResponseDto;
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
import com.onlinemart.order.event.OrderCancelledEvent;
import com.onlinemart.order.outbox.OutboxWriter;
import com.onlinemart.order.dto.response.BrowseMetaDto;
import com.onlinemart.order.helper.BrowseHelper;
import org.springframework.data.jpa.domain.Specification;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;
    private final OrderEventPublisher orderEventPublisher;
    private final CartClientService cartClientService;
    private final OutboxWriter outboxWriter;

    @Value("${spring.kafka.topic.order.created}")
    private String orderCreatedTopic;

    @Value("${spring.kafka.topic.order.cancelled}")
    private String orderCancelledTopic;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            OrderMapper orderMapper,
            OrderEventPublisher orderEventPublisher,
            CartClientService cartClientService,
            OutboxWriter outboxWriter) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderMapper = orderMapper;
        this.orderEventPublisher = orderEventPublisher;
        this.cartClientService = cartClientService;
        this.outboxWriter = outboxWriter;
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

            // orderEventPublisher.publishOrderCreated(event);
            outboxWriter.write(
                    savedOrder.getId().toString(),
                    "ORDER_CREATED",
                    orderCreatedTopic,
                    event
            );

            return OrderResponseDto.builder()
                    .success(Boolean.TRUE)
                    .message("Order received and is being processed")
                    .data(orderMapper.toSaveResponseDto(savedOrder, orderItems).getData())
                    .build();

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

            if ("CANCELLED".equalsIgnoreCase(requestDto.getStatus())
                    && "DELIVERED".equalsIgnoreCase(order.getStatus())) {
                buildException("Delivered order cannot be cancelled", "ORDER_CANCELLED_NOT_ALLOWED");
            }

            String previousStatus = order.getStatus();
            order.setStatus(requestDto.getStatus());
            orderRepository.save(order);

            // Publish cancellation event to restore inventory
            if ("CANCELLED".equalsIgnoreCase(requestDto.getStatus())) {
                List<OrderItems> orderItems = orderItemRepository.findByOrderId(order.getId());

                List<OrderItemEvent> itemEvents = orderItems.stream()
                        .map(oi -> new OrderItemEvent(oi.getProductId(), oi.getQuantity(), oi.getUnitPrice()))
                        .toList();

                OrderCancelledEvent cancelledEvent = new OrderCancelledEvent(
                        order.getId(),
                        order.getCustomerId(),
                        order.getCartId(),
                        itemEvents
                );

                // orderEventPublisher.publishOrderCancelled(cancelledEvent);
                outboxWriter.write(
                        order.getId().toString(),
                        "ORDER_CANCELLED",
                        orderCancelledTopic,
                        cancelledEvent
                );
                log.info("Published {} for orderId={} previousStatus={}", "${spring.kafka.topic.order.cancelled}", order.getId(), previousStatus);
            }

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

    @Override
    public BrowseResponseDto<OrderBrowseResponseDto> browse(BrowseRequestDto req) {
        Specification<Order> spec = BrowseHelper.buildSpecification(req.getFilters());
        Pageable pageable = BrowseHelper.buildPageable(req);

        Page<Order> resultPage = orderRepository.findAll(spec, pageable);

        List<OrderBrowseResponseDto> data = resultPage.getContent()
                .stream()
                .map(orderMapper::toOrderBrowseDto)
                .collect(Collectors.toList());

        BrowseMetaDto meta = new BrowseMetaDto(
                req.getPage(),
                req.getSize(),
                req.getLimit(),
                resultPage.getTotalElements()
        );

        return new BrowseResponseDto<>(true, "Orders fetched successfully", data, meta);
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