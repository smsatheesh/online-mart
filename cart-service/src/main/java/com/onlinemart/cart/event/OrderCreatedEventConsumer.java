package com.onlinemart.cart.event;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.onlinemart.cart.service.CartService;

@Component
public class OrderCreatedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedEventConsumer.class);

    private final CartService cartService;

    @Value("${spring.kafka.topic.order.created}")
    private String ORDER_CREATED_TOPIC;

    public OrderCreatedEventConsumer(CartService cartService) {
        this.cartService = cartService;
    }

    @KafkaListener(
            topics = "${spring.kafka.topic.order.created}",
            groupId = "${spring.kafka.consumer.group-id}",
            properties = {"spring.json.value.default.type=com.onlinemart.cart.event.OrderCreatedEvent"})
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received {} for orderId={} clearing cartId={}",
                ORDER_CREATED_TOPIC, event.getOrderId(), event.getCartId());
        clearCartWithCircuitBreaker(event);
    }

    @CircuitBreaker(name = "cartClearer", fallbackMethod = "clearCartFallback")
    public void clearCartWithCircuitBreaker(OrderCreatedEvent event) {
        cartService.clearCartItems(event.getCartId());
        log.info("Cart cleared for cartId={}", event.getCartId());
    }

    public void clearCartFallback(OrderCreatedEvent event, Throwable t) {
        log.error("Circuit breaker OPEN — could not clear cartId={} for orderId={}: {}",
                event.getCartId(), event.getOrderId(), t.getMessage());
        throw new RuntimeException("cartClearer circuit breaker is OPEN", t);
    }
}