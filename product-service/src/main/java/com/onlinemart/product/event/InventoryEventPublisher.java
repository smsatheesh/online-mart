package com.onlinemart.product.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.inventory.reserved}")
    private String INVENTORY_RESERVED_TOPIC;

    @Value("${spring.kafka.topic.inventory.failed}")
    private String INVENTORY_RESERVED_FAILED;

    public InventoryEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishReserved(InventoryReservedEvent event) {
        kafkaTemplate.send(INVENTORY_RESERVED_TOPIC, event.getOrderId().toString(), event)
                .whenComplete((r, ex) -> {
                    if (ex != null)
                        log.error("Failed to publish {} for orderId={}", INVENTORY_RESERVED_TOPIC, event.getOrderId(), ex);
                    else log.info("Published {} for orderId={}", INVENTORY_RESERVED_TOPIC, event.getOrderId());
                });
    }

    public void publishFailed(InventoryFailedEvent event) {
        kafkaTemplate.send(INVENTORY_RESERVED_FAILED, event.getOrderId().toString(), event)
                .whenComplete((r, ex) -> {
                    if (ex != null)
                        log.error("Failed to publish {} for orderId={}", INVENTORY_RESERVED_FAILED, event.getOrderId(), ex);
                    else log.info("Published {} for orderId={}", INVENTORY_RESERVED_FAILED, event.getOrderId());
                });
    }

}