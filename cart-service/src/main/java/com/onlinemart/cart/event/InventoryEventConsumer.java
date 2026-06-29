package com.onlinemart.cart.event;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.onlinemart.cart.event.InventoryReservedEvent;
import com.onlinemart.cart.service.CartService;

@Component
public class InventoryEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventConsumer.class);

    private final CartService cartService;

    @Value("${spring.kafka.topic.inventory.reserved}")
    private String INVENTORY_RESERVED_TOPIC;

    public InventoryEventConsumer(CartService cartService) {
        this.cartService = cartService;
    }

    @KafkaListener(
            topics = "${spring.kafka.topic.inventory.reserved}",
            groupId = "${spring.kafka.consumer.group-id}",
            properties = {"spring.json.value.default.type=com.onlinemart.cart.event.InventoryReservedEvent"})
    public void handleInventoryReserved(InventoryReservedEvent event) {
        log.info("Inventory reserved for orderId={} → removing all cart items", event.getOrderId());
        clearCartWithCircuitBreaker(event);
    }

    @CircuitBreaker(name = "cartClearer", fallbackMethod = "clearCartFallback")
    public void clearCartWithCircuitBreaker(InventoryReservedEvent event) {
        cartService.clearCartItems(event.getCartId());
        log.info("Cart cleared for cartId={}", event.getCartId());
    }

    public void clearCartFallback(InventoryReservedEvent event, Throwable t) {
        log.error("Circuit breaker OPEN — could not clear cartId={} for orderId={}: {}",
                event.getCartId(), event.getOrderId(), t.getMessage());
        throw new RuntimeException("cartClearer circuit breaker is OPEN", t);
    }
}