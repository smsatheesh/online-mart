package com.onlinemart.order.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.*;

import com.onlinemart.order.service.OrderService;
import com.onlinemart.order.dto.request.OrderStatusRequestDto;
import com.onlinemart.order.event.*;

@Component
public class InventoryEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventConsumer.class);

    private final OrderService orderService;
    private final OrderEventPublisher orderEventPublisher;

    public InventoryEventConsumer(OrderService orderService,
                                  OrderEventPublisher orderEventPublisher) {
        this.orderService = orderService;
        this.orderEventPublisher = orderEventPublisher;
    }

    @KafkaListener(topics = "${spring.kafka.topic.inventory.reserved}", groupId = "${spring.kafka.consumer.group-id}",
            properties = {"spring.json.value.default.type=com.onlinemart.order.event.InventoryReservedEvent"})
    public void handleInventoryReserved(InventoryReservedEvent event) {
        log.info("Inventory reserved for orderId={} → marking CONFIRMED", event.getOrderId());

        OrderStatusRequestDto requestDto = new OrderStatusRequestDto();
        requestDto.setOrderId(event.getOrderId());
        requestDto.setStatus("CONFIRMED");
        orderService.updateStatusOfOrder(requestDto);
    }

    @KafkaListener(topics = "${spring.kafka.topic.inventory.failed}", groupId = "${spring.kafka.consumer.group-id}",
            properties = {"spring.json.value.default.type=com.onlinemart.order.event.InventoryFailedEvent"})
    public void handleInventoryFailed(InventoryFailedEvent event) {
        log.warn("Inventory failed for orderId={} reason={} → CANCELLED", event.getOrderId(), event.getReason());

        OrderStatusRequestDto requestDto = new OrderStatusRequestDto();
        requestDto.setOrderId(event.getOrderId());
        requestDto.setStatus("CANCELLED");
        orderService.updateStatusOfOrder(requestDto);

        List<CancelledOrderItemEvent> items = orderService.getOrderItems(event.getOrderId());

        orderEventPublisher.publishOrderCancelled(
                new OrderCancelledEvent(event.getOrderId(), event.getCartId(), null, items));
    }

}