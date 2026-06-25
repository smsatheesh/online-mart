package com.onlinemart.order.service;

import com.onlinemart.order.dto.request.CreateOrderRequestDto;
import com.onlinemart.order.dto.request.OrderStatusRequestDto;
import com.onlinemart.order.dto.response.OrderDetailResponseDto;
import com.onlinemart.order.dto.response.OrderDto;
import com.onlinemart.order.dto.response.OrderResponseDto;
import com.onlinemart.order.entity.Order;
import com.onlinemart.order.entity.OrderItems;
import com.onlinemart.order.exception.OrderServiceException;
import com.onlinemart.order.mapper.OrderMapper;
import com.onlinemart.order.repository.OrderItemRepository;
import com.onlinemart.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void saveOrder_shouldCreateOrderSuccessfully() {

        CreateOrderRequestDto request = new CreateOrderRequestDto();
        request.setCartId(1L);
        request.setCustomerId(10L);

        com.onlinemart.order.dto.request.OrderItemsDataDto item =
                new com.onlinemart.order.dto.request.OrderItemsDataDto();
        item.setProductId(100L);
        item.setQuantity(2L);
        item.setUnitPrice(500L);

        request.setItems(List.of(item));

        Order entity = new Order();
        entity.setCustomerId(10L);
        entity.setCartId(1L);

        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setCustomerId(10L);
        savedOrder.setCartId(1L);

        OrderResponseDto response = OrderResponseDto.builder()
                .success(true)
                .message("Order created successfully")
                .build();

        when(orderMapper.toEntity(request)).thenReturn(entity);
        when(orderRepository.save(entity)).thenReturn(savedOrder);
        when(orderMapper.toSaveResponseDto(savedOrder)).thenReturn(response);

        OrderResponseDto result = orderService.saveOrder(request);

        assertNotNull(result);
        assertTrue(result.getSuccess());

        verify(orderRepository).save(entity);
        verify(orderItemRepository).saveAll(anyList());
        verify(orderMapper).toSaveResponseDto(savedOrder);
    }

    @Test
    void saveOrder_shouldThrowDuplicateOrderItemException() {

        CreateOrderRequestDto request = new CreateOrderRequestDto();

        Order entity = new Order();

        when(orderMapper.toEntity(request)).thenReturn(entity);

        when(orderRepository.save(entity))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        OrderServiceException exception =
                assertThrows(
                        OrderServiceException.class,
                        () -> orderService.saveOrder(request)
                );

        assertEquals(
                "DUPLICATE_ORDER_ITEM",
                exception.getErrorResponse().getErrorCode()
        );
    }

    @Test
    void saveOrder_shouldThrowOrderSaveFailedException() {

        CreateOrderRequestDto request = new CreateOrderRequestDto();

        when(orderMapper.toEntity(request))
                .thenThrow(new RuntimeException("Unexpected Error"));

        OrderServiceException exception =
                assertThrows(
                        OrderServiceException.class,
                        () -> orderService.saveOrder(request)
                );

        assertEquals(
                "ORDER_SAVE_FAILED",
                exception.getErrorResponse().getErrorCode()
        );
    }

    @Test
    void fetchOrderDetails_shouldReturnOrderDetails() {

        Order order = new Order();
        order.setId(1L);
        order.setCustomerId(10L);
        order.setCartId(100L);
        order.setStatus("PLACED");

        OrderItems item = new OrderItems();
        item.setQuantity(2L);
        item.setUnitPrice(500L);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderItemRepository.findByOrderId(1L))
                .thenReturn(List.of(item));

        OrderDetailResponseDto result =
                orderService.fetchOrderDetails(1L);

        assertNotNull(result);
        assertTrue(result.getSuccess());

        OrderDto dto = result.getData();

        assertEquals(1L, dto.getOrderId());
        assertEquals(10L, dto.getCustomerId());
        assertEquals(100L, dto.getCartId());
        assertEquals("PLACED", dto.getStatus());
        assertEquals(1000L, dto.getTotalPrice());
    }

    @Test
    void fetchOrderDetails_shouldThrowWhenOrderNotFound() {

        when(orderRepository.findById(1L))
                .thenReturn(Optional.empty());

        OrderServiceException exception =
                assertThrows(
                        OrderServiceException.class,
                        () -> orderService.fetchOrderDetails(1L)
                );

        assertEquals(
                "ORDER_NOT_FOUND",
                exception.getErrorResponse().getErrorCode()
        );
    }

    @Test
    void fetchOrderDetails_shouldThrowFetchFailedException() {

        when(orderRepository.findById(1L))
                .thenThrow(new RuntimeException("Database Error"));

        OrderServiceException exception =
                assertThrows(
                        OrderServiceException.class,
                        () -> orderService.fetchOrderDetails(1L)
                );

        assertEquals(
                "ORDER_FETCH_FAILED",
                exception.getErrorResponse().getErrorCode()
        );
    }

    @Test
    void updateStatusOfOrder_shouldUpdateSuccessfully() {

        OrderStatusRequestDto request = new OrderStatusRequestDto();
        request.setOrderId(1L);
        request.setStatus("SHIPPED");

        Order order = new Order();
        order.setId(1L);
        order.setCustomerId(10L);
        order.setCartId(100L);
        order.setStatus("PLACED");

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);

        when(orderItemRepository.findByOrderId(1L))
                .thenReturn(Collections.emptyList());

        OrderDetailResponseDto result =
                orderService.updateStatusOfOrder(request);

        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertEquals(
                "SHIPPED",
                result.getData().getStatus()
        );

        verify(orderRepository).save(order);
    }

    @Test
    void updateStatusOfOrder_shouldThrowWhenOrderNotFound() {

        OrderStatusRequestDto request = new OrderStatusRequestDto();
        request.setOrderId(1L);
        request.setStatus("SHIPPED");

        when(orderRepository.findById(1L))
                .thenReturn(Optional.empty());

        OrderServiceException exception =
                assertThrows(
                        OrderServiceException.class,
                        () -> orderService.updateStatusOfOrder(request)
                );

        assertEquals(
                "ORDER_NOT_FOUND",
                exception.getErrorResponse().getErrorCode()
        );
    }

    @Test
    void updateStatusOfOrder_shouldThrowStatusUpdateFailedException() {

        OrderStatusRequestDto request = new OrderStatusRequestDto();
        request.setOrderId(1L);
        request.setStatus("SHIPPED");

        when(orderRepository.findById(1L))
                .thenThrow(new RuntimeException("Database Error"));

        OrderServiceException exception =
                assertThrows(
                        OrderServiceException.class,
                        () -> orderService.updateStatusOfOrder(request)
                );

        assertEquals(
                "STATUS_UPDATE_FAILED",
                exception.getErrorResponse().getErrorCode()
        );
    }
}