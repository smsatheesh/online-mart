package com.onlinemart.product.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.onlinemart.product.event.*;
import com.onlinemart.product.service.*;

@Component
public class OrderCreatedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedEventConsumer.class);

    private final ProductService productService;

    public OrderCreatedEventConsumer(ProductService productService) {
        this.productService = productService;
    }

    @KafkaListener(topics = "order.created", groupId = "product-service-group")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received order.created for orderId={}", event.getOrderId());

        event.getItems().forEach(item -> {
            try {
                productService.deductStock(item.getProductId(), item.getQuantity());
                log.info("Deducted stock: productId={} qty={}", item.getProductId(), item.getQuantity());
            } catch (Exception e) {
                log.error("Failed to deduct stock for productId={}", item.getProductId(), e);
                // TODO: publish inventory.failed event for Saga compensation
            }
        });
    }
}