package com.onlinemart.product.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import com.onlinemart.product.event.*;
import com.onlinemart.product.service.*;

@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final ProductService productService;

    public OrderEventConsumer(ProductService productService) {
        this.productService = productService;
    }

    @Value("${spring.kafka.topic.order.created}")
    private String ORDER_CREATED_TOPIC;

    @Value("${spring.kafka.topic.order.cancelled}")
    private String ORDER_CANCELLED_TOPIC;

    @KafkaListener(
            topics = "${spring.kafka.topic.order.created}",
            groupId = "${spring.kafka.consumer.group-id}",
            properties = {"spring.json.value.default.type=com.onlinemart.product.event.OrderCreatedEvent"})
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received {} for orderId={}", ORDER_CREATED_TOPIC, event.getOrderId());

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

    @KafkaListener(
            topics = "${spring.kafka.topic.order.cancelled}",
            groupId = "${spring.kafka.consumer.group-id}",
            properties = {"spring.json.value.default.type=com.onlinemart.product.event.OrderCancelledEvent"})
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("Received {} for orderId={}", ORDER_CANCELLED_TOPIC, event.getOrderId());

        for(OrderItemEvent item: event.getItems()) {
            try {
                productService.restoreStock(item.getProductId(), item.getQuantity());
                log.info("Restored stock: productId={} qty={}", item.getProductId(), item.getQuantity());
            } catch (Exception e) {
                log.error("Failed to restore stock for productId={}", item.getProductId(), e);
            }
        }
    }

}