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
    private String orderCreatedTopic;

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public OrderEventPublisher(KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderCreated(OrderCreatedEvent event) {
        // key = userId string so all events for same user go to same partition
        String key = event.getCustomerId().toString();

        CompletableFuture<SendResult<String, OrderCreatedEvent>> future =
                kafkaTemplate.send(orderCreatedTopic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish {} for orderId={}", orderCreatedTopic, event.getOrderId(), ex);
            } else {
                log.info("Published {} for orderId={} to partition={} offset={}",
                        orderCreatedTopic,
                        event.getOrderId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}