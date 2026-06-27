package com.onlinemart.cart.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.onlinemart.cart.service.*;

@Component
public class OrderCreatedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedEventConsumer.class);

    private final CartService cartService;

    public OrderCreatedEventConsumer(CartService cartService) {
        this.cartService = cartService;
    }

    @KafkaListener(topics = "order.created", groupId = "cart-service-group")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received order.created for orderId={}, clearing cartId={}",
                event.getOrderId(), event.getCartId());

        try {
            cartService.clearCartItems(event.getCartId());
            log.info("Cart cleared for cartId={}", event.getCartId());
        } catch (Exception e) {
            log.error("Failed to clear cart for cartId={}", event.getCartId(), e);
        }
    }
}