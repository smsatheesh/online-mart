package com.onlinemart.order.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import java.util.concurrent.CompletableFuture;

@Component
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

    @Value("${spring.kafka.topic.order.created}")
    private String ORDER_CREATED_TOPIC;

    @Value("${spring.kafka.topic.order.failed}")
    private String ORDER_FAILED_TOPIC;

    @Value("${spring.kafka.topic.order.cancelled}")
    private String ORDER_CANCELLED_TOPIC;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderCreated(OrderCreatedEvent event) {
        String key = event.getCustomerId().toString();

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(ORDER_CREATED_TOPIC, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish {} for orderId={}", ORDER_CREATED_TOPIC, event.getOrderId(), ex);
            } else {
                log.info("Published {} for orderId={} to partition={} offset={}",
                        ORDER_CREATED_TOPIC,
                        event.getOrderId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }

    public void publishOrderFailed(OrderFailedEvent event) {
        kafkaTemplate.send(ORDER_FAILED_TOPIC, event.getOrderId().toString(), event)
                .whenComplete((r, ex) -> {
                    if (ex != null) log.error("Failed to publish {}", ORDER_FAILED_TOPIC, ex);
                    else log.info("Published {} for orderId={}", ORDER_FAILED_TOPIC, event.getOrderId());
                });
    }

    public void publishOrderCancelled(OrderCancelledEvent event) {
        kafkaTemplate.send(ORDER_CANCELLED_TOPIC, event.getOrderId().toString(), event)
                .whenComplete((r, ex) -> {
                   if (ex != null) log.error("Failed to publish {}", ORDER_CANCELLED_TOPIC, ex);
                   else log.info("Published {} for orderId={}", ORDER_CANCELLED_TOPIC, event.getOrderId());
                });
    }

}