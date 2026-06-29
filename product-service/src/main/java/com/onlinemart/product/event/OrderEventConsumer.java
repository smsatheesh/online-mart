package com.onlinemart.product.event;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.onlinemart.product.event.OrderCancelledEvent;
import com.onlinemart.product.event.OrderCreatedEvent;
import com.onlinemart.product.event.OrderItemEvent;
import com.onlinemart.product.service.ProductService;

@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final ProductService productService;

    @Value("${spring.kafka.topic.order.created}")
    private String ORDER_CREATED_TOPIC;

    @Value("${spring.kafka.topic.order.cancelled}")
    private String ORDER_CANCELLED_TOPIC;

    public OrderEventConsumer(ProductService productService) {
        this.productService = productService;
    }

    @KafkaListener(
            topics = "${spring.kafka.topic.order.created}",
            groupId = "${spring.kafka.consumer.group-id}",
            properties = {"spring.json.value.default.type=com.onlinemart.product.event.OrderCreatedEvent"})
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received {} for orderId={}", ORDER_CREATED_TOPIC, event.getOrderId());

        for (OrderItemEvent item : event.getItems()) {
            deductStockWithCircuitBreaker(item, event);
        }
    }

    @CircuitBreaker(name = "inventoryProcessor", fallbackMethod = "deductStockFallback")
    public void deductStockWithCircuitBreaker(OrderItemEvent item, OrderCreatedEvent event) {
        productService.deductStock(
                item.getProductId(),
                item.getQuantity(),
                event.getOrderId(),
                event.getCartId()
        );
        log.info("Deducted stock: productId={} qty={}", item.getProductId(), item.getQuantity());
    }

    public void deductStockFallback(OrderItemEvent item, OrderCreatedEvent event, Throwable t) {
        log.error("Circuit breaker OPEN — skipping deductStock for productId={} orderId={}: {}",
                item.getProductId(), event.getOrderId(), t.getMessage());
        throw new RuntimeException("inventoryProcessor circuit breaker is OPEN", t);
    }

    @KafkaListener(
            topics = "${spring.kafka.topic.order.cancelled}",
            groupId = "${spring.kafka.consumer.group-id}",
            properties = {"spring.json.value.default.type=com.onlinemart.product.event.OrderCancelledEvent"})
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("Received {} for orderId={}", ORDER_CANCELLED_TOPIC, event.getOrderId());

        for (OrderItemEvent item : event.getItems()) {
            restoreStockWithCircuitBreaker(item, event);
        }
    }

    @CircuitBreaker(name = "inventoryProcessor", fallbackMethod = "restoreStockFallback")
    public void restoreStockWithCircuitBreaker(OrderItemEvent item, OrderCancelledEvent event) {
        productService.restoreStock(item.getProductId(), item.getQuantity());
        log.info("Restored stock: productId={} qty={}", item.getProductId(), item.getQuantity());
    }

    public void restoreStockFallback(OrderItemEvent item, OrderCancelledEvent event, Throwable t) {
        log.error("Circuit breaker OPEN — skipping restoreStock for productId={} orderId={}: {}",
                item.getProductId(), event.getOrderId(), t.getMessage());
        throw new RuntimeException("inventoryProcessor circuit breaker is OPEN", t);
    }
}