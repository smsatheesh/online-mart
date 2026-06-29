package com.onlinemart.order.service;

import com.onlinemart.order.client.CartClientService;
import com.onlinemart.order.client.dto.response.CartItemsDataDto;
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
import com.onlinemart.order.event.*;
import com.onlinemart.order.outbox.OutboxWriter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private CartClientService cartClientService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @Mock
    private OutboxWriter outboxWriter;

    // ─── setup ───────────────────────────────────────────────────────────────
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orderService, "orderCreatedTopic", "local.sales.ecommerce.order.created.v1");
    }

    // ─── helper: build a CartItemsDataDto ────────────────────────────────────

    private CartItemsDataDto cartItem(Long productId, Long quantity, Long unitPrice) {
        CartItemsDataDto item = new CartItemsDataDto();
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        return item;
    }

    // ─── saveOrder ───────────────────────────────────────────────────────────

    @Test
    void saveOrder_shouldCreateOrderSuccessfully() {

        CreateOrderRequestDto request = new CreateOrderRequestDto();
        request.setCartId(1L);
        request.setCustomerId(10L);

        List<CartItemsDataDto> cartItems = List.of(cartItem(100L, 2L, 500L));

        Order entity = new Order();
        entity.setCustomerId(10L);
        entity.setCartId(1L);
        entity.setTotalAmount(1000L);

        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setCustomerId(10L);
        savedOrder.setCartId(1L);
        savedOrder.setTotalAmount(1000L);

        OrderResponseDto response = OrderResponseDto.builder()
                .success(true)
                .message("Order received and is being processed")
                .build();

        when(cartClientService.fetchCartItems(1L)).thenReturn(cartItems);
        when(orderMapper.toEntity(request, 1000L)).thenReturn(entity);
        when(orderRepository.save(entity)).thenReturn(savedOrder);
        when(orderMapper.toSaveResponseDto(eq(savedOrder), anyList())).thenReturn(response);

        // outboxWriter.write() is void — doNothing is the explicit stub
        doNothing().when(outboxWriter).write(anyString(), anyString(), anyString(), any());

        OrderResponseDto result = orderService.saveOrder(request);

        assertNotNull(result);
        assertTrue(result.getSuccess());

        verify(cartClientService).fetchCartItems(1L);
        verify(orderMapper).toEntity(request, 1000L);
        verify(orderRepository).save(entity);
        verify(orderItemRepository).saveAll(anyList());
        verify(outboxWriter).write(
                eq("1"),
                eq("ORDER_CREATED"),
                eq("local.sales.ecommerce.order.created.v1"),
                any()
        );
        verify(orderMapper).toSaveResponseDto(eq(savedOrder), anyList());
    }

    @Test
    void saveOrder_shouldThrowWhenCartIsEmpty() {

        CreateOrderRequestDto request = new CreateOrderRequestDto();
        request.setCartId(1L);
        request.setCustomerId(10L);

        when(cartClientService.fetchCartItems(1L)).thenReturn(Collections.emptyList());

        OrderServiceException exception = assertThrows(
                OrderServiceException.class,
                () -> orderService.saveOrder(request)
        );

        assertEquals("ORDER_ITEM_NOT_FOUND", exception.getErrorResponse().getErrorCode());

        verify(orderRepository, never()).save(any());
        verify(orderItemRepository, never()).saveAll(anyList());
    }

    @Test
    void saveOrder_shouldThrowDuplicateOrderItemException() {

        CreateOrderRequestDto request = new CreateOrderRequestDto();
        request.setCartId(1L);
        request.setCustomerId(10L);

        List<CartItemsDataDto> cartItems = List.of(cartItem(100L, 2L, 500L));

        Order entity = new Order();

        when(cartClientService.fetchCartItems(1L)).thenReturn(cartItems);
        when(orderMapper.toEntity(eq(request), anyLong())).thenReturn(entity);
        when(orderRepository.save(entity))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        OrderServiceException exception = assertThrows(
                OrderServiceException.class,
                () -> orderService.saveOrder(request)
        );

        assertEquals("DUPLICATE_ORDER_ITEM", exception.getErrorResponse().getErrorCode());
    }

    @Test
    void saveOrder_shouldThrowOrderSaveFailedException() {

        CreateOrderRequestDto request = new CreateOrderRequestDto();
        request.setCartId(1L);
        request.setCustomerId(10L);

        when(cartClientService.fetchCartItems(1L))
                .thenThrow(new RuntimeException("Feign connection refused"));

        OrderServiceException exception = assertThrows(
                OrderServiceException.class,
                () -> orderService.saveOrder(request)
        );

        assertEquals("ORDER_SAVE_FAILED", exception.getErrorResponse().getErrorCode());
    }

    // ─── fetchOrderDetails ───────────────────────────────────────────────────

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

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(item));

        OrderDetailResponseDto result = orderService.fetchOrderDetails(1L);

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

        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        OrderServiceException exception = assertThrows(
                OrderServiceException.class,
                () -> orderService.fetchOrderDetails(1L)
        );

        assertEquals("ORDER_NOT_FOUND", exception.getErrorResponse().getErrorCode());
    }

    @Test
    void fetchOrderDetails_shouldThrowFetchFailedException() {

        when(orderRepository.findById(1L)).thenThrow(new RuntimeException("Database Error"));

        OrderServiceException exception = assertThrows(
                OrderServiceException.class,
                () -> orderService.fetchOrderDetails(1L)
        );

        assertEquals("ORDER_FETCH_FAILED", exception.getErrorResponse().getErrorCode());
    }

    // ─── updateStatusOfOrder ─────────────────────────────────────────────────

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

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Collections.emptyList());

        OrderDetailResponseDto result = orderService.updateStatusOfOrder(request);

        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertEquals("SHIPPED", result.getData().getStatus());

        verify(orderRepository).save(order);
    }

    @Test
    void updateStatusOfOrder_shouldThrowWhenOrderNotFound() {

        OrderStatusRequestDto request = new OrderStatusRequestDto();
        request.setOrderId(1L);
        request.setStatus("SHIPPED");

        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        OrderServiceException exception = assertThrows(
                OrderServiceException.class,
                () -> orderService.updateStatusOfOrder(request)
        );

        assertEquals("ORDER_NOT_FOUND", exception.getErrorResponse().getErrorCode());
    }

    @Test
    void updateStatusOfOrder_shouldThrowStatusUpdateFailedException() {

        OrderStatusRequestDto request = new OrderStatusRequestDto();
        request.setOrderId(1L);
        request.setStatus("SHIPPED");

        when(orderRepository.findById(1L)).thenThrow(new RuntimeException("Database Error"));

        OrderServiceException exception = assertThrows(
                OrderServiceException.class,
                () -> orderService.updateStatusOfOrder(request)
        );

        assertEquals("STATUS_UPDATE_FAILED", exception.getErrorResponse().getErrorCode());
    }
}