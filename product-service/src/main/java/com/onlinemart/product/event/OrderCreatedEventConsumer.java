package com.onlinemart.product.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import com.onlinemart.product.event.*;
import com.onlinemart.product.service.*;
import com.onlinemart.product.event.*;

@Component
public class OrderCreatedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedEventConsumer.class);

    private final ProductService productService;

    public OrderCreatedEventConsumer(ProductService productService) {
        this.productService = productService;
    }

    @KafkaListener(topics = "${spring.kafka.topic.order.created}", groupId = "${spring.kafka.consumer.group-id}",
            properties = {"spring.json.value.default.type=com.onlinemart.product.event.OrderCreatedEvent"})
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received order.created for orderId={}", event.getOrderId());

        for (OrderItemEvent item : event.getItems()) {
            try {
                productService.deductStock(
                        item.getProductId(),
                        item.getQuantity(),
                        event.getOrderId(),
                        event.getCartId()
                );
                log.info("Deducted stock: productId={} qty={}", item.getProductId(), item.getQuantity());
            } catch (Exception e) {
                log.error("Failed to deduct stock for productId={}", item.getProductId(), e);
            }
        }
    }

}