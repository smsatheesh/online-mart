package com.onlinemart.cart.event;

import com.onlinemart.cart.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCancelledEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderCancelledEventConsumer.class);
    private final CartService cartService;

    public OrderCancelledEventConsumer(CartService cartService) {
        this.cartService = cartService;
    }

    @KafkaListener(topics = "${spring.kafka.topic.order.cancelled}", groupId = "${spring.kafka.consumer.group-id}",
            properties = {"spring.json.value.default.type=com.onlinemart.cart.event.OrderCancelledEvent"})
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.warn("Order cancelled orderId={} → restoring {} items to cartId={}",
                event.getOrderId(), event.getItems().size(), event.getCartId());
        cartService.restoreCart(event.getCartId(), event.getItems());
    }
}