package com.onlinemart.order.event;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.onlinemart.order.dto.request.OrderStatusRequestDto;
import com.onlinemart.order.service.OrderService;

@Component
public class InventoryEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventConsumer.class);

    private final OrderService orderService;

    public InventoryEventConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(
            topics = "${spring.kafka.topic.inventory.reserved}",
            groupId = "${spring.kafka.consumer.group-id}",
            properties = {"spring.json.value.default.type=com.onlinemart.order.event.InventoryReservedEvent"})
    public void handleInventoryReserved(InventoryReservedEvent event) {
        log.info("Inventory reserved for orderId={} → marking CONFIRMED", event.getOrderId());
        confirmOrderWithCircuitBreaker(event);
    }

    @CircuitBreaker(name = "orderStatusUpdater", fallbackMethod = "confirmOrderFallback")
    public void confirmOrderWithCircuitBreaker(InventoryReservedEvent event) {
        OrderStatusRequestDto dto = new OrderStatusRequestDto();
        dto.setOrderId(event.getOrderId());
        dto.setStatus("CONFIRMED");
        orderService.updateStatusOfOrder(dto);
    }

    public void confirmOrderFallback(InventoryReservedEvent event, Throwable t) {
        log.error("Circuit breaker OPEN — could not confirm orderId={}: {}",
                event.getOrderId(), t.getMessage());
        throw new RuntimeException("orderStatusUpdater circuit breaker is OPEN", t);
    }

    @KafkaListener(
            topics = "${spring.kafka.topic.inventory.failed}",
            groupId = "${spring.kafka.consumer.group-id}",
            properties = {"spring.json.value.default.type=com.onlinemart.order.event.InventoryFailedEvent"})
    public void handleInventoryFailed(InventoryFailedEvent event) {
        log.warn("Inventory failed for orderId={} reason={} → FAILED", event.getOrderId(), event.getReason());
        failOrderWithCircuitBreaker(event);
    }

    @CircuitBreaker(name = "orderStatusUpdater", fallbackMethod = "failOrderFallback")
    public void failOrderWithCircuitBreaker(InventoryFailedEvent event) {
        OrderStatusRequestDto dto = new OrderStatusRequestDto();
        dto.setOrderId(event.getOrderId());
        dto.setStatus("FAILED");
        orderService.updateStatusOfOrder(dto);
    }

    public void failOrderFallback(InventoryFailedEvent event, Throwable t) {
        log.error("Circuit breaker OPEN — could not fail orderId={}: {}",
                event.getOrderId(), t.getMessage());
        throw new RuntimeException("orderStatusUpdater circuit breaker is OPEN", t);
    }
}